package com.korbit.deliverytrackingapp.data.sync

import com.korbit.deliverytrackingapp.core.logging.AppLogger
import com.korbit.deliverytrackingapp.data.local.dao.TaskActionEventDao
import com.korbit.deliverytrackingapp.data.local.entity.TaskActionEventEntity
import com.korbit.deliverytrackingapp.data.remote.api.DeliveryApi
import com.korbit.deliverytrackingapp.data.remote.dto.TaskActionRequestDto
import kotlinx.coroutines.delay
import java.io.IOException
import javax.inject.Inject
import kotlin.math.pow

/**
 * Orchestrates outbox sync: fetches pending events (max 50), sends to server,
 * marks synced only on success, handles partial failure, retries with exponential backoff.
 * All logging is structured (key=value) for parsing and monitoring.
 */
class SyncOrchestrator @Inject constructor(
    private val taskActionEventDao: TaskActionEventDao,
    private val api: DeliveryApi,
    private val syncConfig: SyncConfig,
    private val logger: AppLogger
) {
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
     * Sends the batch to the server. Marks synced only for successful responses;
     * on failure increments retry and marks failed (partial failure handling).
     */
    private suspend fun processBatch(events: List<TaskActionEventEntity>): SyncOrchestratorResult {
        var synced = 0
        var failed = 0

        for (event in events) {
            val request = TaskActionRequestDto(
                taskId = event.taskId,
                action = event.action,
                payload = event.payload.ifEmpty { null }
            )
            val response = runCatching { api.submitTaskAction(request) }.getOrElse { throw it }

            if (response.isSuccessful) {
                taskActionEventDao.markSynced(eventId = event.id, syncedAt = System.currentTimeMillis())
                synced++
                logStructured(
                    "event_synced",
                    "eventId" to event.id,
                    "taskId" to event.taskId,
                    "action" to event.action
                )
            } else {
                taskActionEventDao.incrementRetry(event.id)
                taskActionEventDao.markFailed(
                    eventId = event.id,
                    reason = "http_${response.code()}_${response.message().orEmpty()}"
                )
                failed++
                logStructured(
                    "event_failed",
                    "eventId" to event.id,
                    "taskId" to event.taskId,
                    "code" to response.code(),
                    "retryCount" to (event.retryCount + 1)
                )
            }
        }

        return SyncOrchestratorResult(synced = synced, failed = failed, skipped = 0)
    }

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
