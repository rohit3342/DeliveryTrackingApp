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
        logger.d(tag, "Processing ${pending.size} pending outbox events")
        var synced = 0
        for (event in pending) {
            val result = api.submitTaskAction(
                TaskActionRequestDto(
                    taskId = event.taskId,
                    action = event.action,
                    payload = event.payload.ifEmpty { null }
                )
            )
            if (result.isSuccessful) {
                outboxRepository.markSynced(event.id, System.currentTimeMillis())
                synced++
            } else {
                outboxRepository.markFailed(event.id, result.code().toString())
                logger.w(tag, "Task action failed: ${event.taskId} code=${result.code()}")
            }
        }
        synced
    }.onFailure {
        logger.e(tag, "Outbox processing failed", it)
    }
}
