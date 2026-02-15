package com.korbit.deliverytrackingapp.data.sync

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.korbit.deliverytrackingapp.core.logging.AppLogger
import com.korbit.deliverytrackingapp.data.local.dao.TaskActionEventDao
import com.korbit.deliverytrackingapp.data.local.entity.TaskActionEventEntity
import com.korbit.deliverytrackingapp.data.remote.api.DeliveryApi
import com.korbit.deliverytrackingapp.data.remote.dto.DeliveryDto
import com.korbit.deliverytrackingapp.data.remote.dto.TaskActionRequestDto
import com.korbit.deliverytrackingapp.data.remote.dto.TaskDto
import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import kotlinx.coroutines.delay
import java.io.IOException
import javax.inject.Inject
import kotlin.math.pow

/** Payload shape for CREATE_PICKUP outbox event (has deliveryId to fetch from Room). */
private data class CreatePickupPayload(@SerializedName("deliveryId") val deliveryId: String)

/**
 * Orchestrates outbox sync: fetches pending events (max 50), sends to server,
 * marks synced only on success, handles partial failure, retries with exponential backoff.
 * CREATE_PICKUP events call createTask API; status actions call submitTaskActions batch.
 */
class SyncOrchestrator @Inject constructor(
    private val taskActionEventDao: TaskActionEventDao,
    private val deliveryRepository: DeliveryRepository,
    private val api: DeliveryApi,
    private val syncConfig: SyncConfig,
    private val logger: AppLogger
) {
    private val gson = Gson()
    private val tag = "SyncOrchestrator"

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 5
        private const val BASE_BACKOFF_MS = 1000L
    }

    /**
     * Runs one sync cycle: fetch up to batchSize pending events, batch send, mark synced only after success.
     * Handles partial failure per event; uses exponential backoff on transient (network) errors.
     */
    suspend fun syncPendingEvents(): Result<SyncOrchestratorResult> = runCatching {
        val batchSize = syncConfig.outboxBatchSize
        val pending = taskActionEventDao.getPendingEvents(limit = batchSize)
        if (pending.isEmpty()) {
            logStructured("batch_empty", "size" to 0)
            return@runCatching SyncOrchestratorResult(synced = 0, failed = 0, skipped = 0)
        }

        logStructured("batch_start", "size" to pending.size, "max" to batchSize)
        var attempt = 0
        var lastException: Throwable? = null

        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                val result = processBatch(pending)
                logStructured(
                    "batch_complete",
                    "synced" to result.synced,
                    "failed" to result.failed,
                    "attempt" to (attempt + 1)
                )
                return@runCatching result
            } catch (e: IOException) {
                lastException = e
                attempt++
                logStructured(
                    "batch_transient_error",
                    "attempt" to attempt,
                    "maxAttempts" to MAX_RETRY_ATTEMPTS,
                    "error" to (e.message ?: "IOException")
                )
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    val backoffMs = (BASE_BACKOFF_MS * 2.0.pow(attempt - 1)).toLong()
                    logStructured("backoff_wait", "delayMs" to backoffMs)
                    delay(backoffMs)
                }
            }
        }

        logStructured("batch_exhausted", "attempts" to MAX_RETRY_ATTEMPTS)
        throw lastException ?: IOException("Sync failed after $MAX_RETRY_ATTEMPTS attempts")
    }.onFailure { e ->
        logStructured("batch_failed", "error" to (e.message ?: e.javaClass.simpleName))
        logger.e(tag, structuredMessage("batch_failed", "error" to (e.message ?: "")), e)
    }

    /**
     * Processes batch: CREATE_PICKUP events go to createTask API (fetch delivery from Room);
     * other events go to submitTaskActions batch.
     */
    private suspend fun processBatch(events: List<TaskActionEventEntity>): SyncOrchestratorResult {
        if (events.isEmpty()) return SyncOrchestratorResult(synced = 0, failed = 0, skipped = 0)

        val (createEvents, actionEvents) = events.partition { it.action == "CREATE_PICKUP" }
        var synced = 0
        var failed = 0

        // 1. Sync created tasks via createTask API (one call per create)
        for (event in createEvents) {
            val deliveryId = runCatching { gson.fromJson(event.payload, CreatePickupPayload::class.java)?.deliveryId }.getOrNull()
            if (deliveryId == null) {
                taskActionEventDao.markFailed(event.id, "invalid_payload")
                failed++
                logStructured("create_failed", "eventId" to event.id, "reason" to "missing_deliveryId")
                continue
            }
            val delivery = deliveryRepository.getDeliveryById(deliveryId)
            if (delivery == null) {
                taskActionEventDao.markFailed(event.id, "delivery_not_found")
                failed++
                logStructured("create_failed", "eventId" to event.id, "deliveryId" to deliveryId, "reason" to "not_found")
                continue
            }
            val dto = deliveryToDto(delivery)
            val response = runCatching { api.createTask(dto) }.getOrElse { throw it }
            if (response.isSuccessful) {
                taskActionEventDao.markSynced(eventId = event.id, syncedAt = System.currentTimeMillis())
                synced++
                logStructured("event_synced", "eventId" to event.id, "taskId" to event.taskId, "action" to event.action)
            } else {
                taskActionEventDao.incrementRetry(event.id)
                taskActionEventDao.markFailed(event.id, "http_${response.code()}_${response.message().orEmpty()}")
                failed++
                logStructured("create_failed", "eventId" to event.id, "code" to response.code())
            }
        }

        // 2. Sync status actions in one batch
        if (actionEvents.isNotEmpty()) {
            val actions = actionEvents.map { e ->
                TaskActionRequestDto(
                    taskId = e.taskId,
                    action = e.action,
                    payload = e.payload.ifEmpty { null },
                    actionTakenAt = if (e.actionTakenAt > 0) e.actionTakenAt else e.createdAt
                )
            }
            val response = runCatching { api.submitTaskActions(actions) }.getOrElse { throw it }
            val now = System.currentTimeMillis()
            if (response.isSuccessful) {
                actionEvents.forEach { e ->
                    taskActionEventDao.markSynced(eventId = e.id, syncedAt = now)
                    logStructured("event_synced", "eventId" to e.id, "taskId" to e.taskId, "action" to e.action)
                }
                synced += actionEvents.size
            } else {
                actionEvents.forEach { e ->
                    taskActionEventDao.incrementRetry(e.id)
                    taskActionEventDao.markFailed(e.id, "http_${response.code()}_${response.message().orEmpty()}")
                    logStructured("event_failed", "eventId" to e.id, "taskId" to e.taskId, "code" to response.code())
                }
                failed += actionEvents.size
            }
        }

        return SyncOrchestratorResult(synced = synced, failed = failed, skipped = 0)
    }

    private fun deliveryToDto(d: Delivery): DeliveryDto =
        DeliveryDto(
            id = d.id,
            riderId = d.riderId,
            status = d.status,
            customerName = d.customerName,
            customerAddress = d.customerAddress,
            customerPhone = d.customerPhone.ifEmpty { null },
            warehouseName = d.warehouseName.ifEmpty { null },
            warehouseAddress = d.warehouseAddress.ifEmpty { null },
            lastUpdatedAt = d.lastUpdatedAt,
            tasks = d.tasks.map { t ->
                TaskDto(
                    id = t.id,
                    type = t.type,
                    status = t.status,
                    sequence = t.sequence,
                    completedAt = t.completedAt,
                    createdAt = t.createdAt,
                    lastModifiedAt = t.lastModifiedAt
                )
            }
        )

    private fun logStructured(event: String, vararg pairs: Pair<String, Any>) {
        val msg = structuredMessage(event, *pairs)
        logger.d(tag, msg)
    }

    private fun structuredMessage(event: String, vararg pairs: Pair<String, Any>): String =
        buildString {
            append("event=$event")
            pairs.forEach { (k, v) -> append(" $k=$v") }
        }

    data class SyncOrchestratorResult(
        val synced: Int,
        val failed: Int,
        val skipped: Int
    )
}
