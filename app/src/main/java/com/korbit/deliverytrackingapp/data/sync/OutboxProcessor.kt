package com.korbit.deliverytrackingapp.data.sync

import com.korbit.deliverytrackingapp.core.logging.AppLogger
import com.korbit.deliverytrackingapp.data.remote.api.DeliveryApi
import com.korbit.deliverytrackingapp.data.remote.dto.TaskActionRequestDto
import com.korbit.deliverytrackingapp.domain.repository.OutboxRepository
import javax.inject.Inject

/**
 * Processes PENDING outbox events: calls API and marks SYNCED or FAILED.
 * Used only by SyncEngine (no direct UI → network).
 */
class OutboxProcessor @Inject constructor(
    private val outboxRepository: OutboxRepository,
    private val api: DeliveryApi,
    private val logger: AppLogger
) {
    private val tag = "OutboxProcessor"

    suspend fun processPending(): Result<Int> = runCatching {
        val pending = outboxRepository.getPendingEvents()
        if (pending.isEmpty()) return@runCatching 0
        logger.d(tag, "Processing ${pending.size} pending outbox events (batch)")
        val actions = pending.map { event ->
            TaskActionRequestDto(
                taskId = event.taskId,
                action = event.action,
                payload = event.payload.ifEmpty { null },
                actionTakenAt = System.currentTimeMillis()
            )
        }
        val result = api.submitTaskActions(actions)
        val now = System.currentTimeMillis()
        if (result.isSuccessful) {
            pending.forEach { outboxRepository.markSynced(it.id, now) }
            pending.size
        } else {
            pending.forEach { outboxRepository.markFailed(it.id, result.code().toString()) }
            logger.w(tag, "Task actions batch failed: code=${result.code()}")
            0
        }
    }.onFailure {
        logger.e(tag, "Outbox processing failed", it)
    }
}
