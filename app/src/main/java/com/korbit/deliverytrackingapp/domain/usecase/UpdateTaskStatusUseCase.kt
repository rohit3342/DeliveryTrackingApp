package com.korbit.deliverytrackingapp.domain.usecase

import com.korbit.deliverytrackingapp.domain.model.TaskAction
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import com.korbit.deliverytrackingapp.domain.repository.OutboxRepository
import javax.inject.Inject

/**
 * All writes go to Room first: update task in DB and enqueue action in Outbox.
 * Sync engine will push Outbox PENDING events to API later.
 */
class UpdateTaskStatusUseCase @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val outboxRepository: OutboxRepository
) {
    suspend operator fun invoke(
        taskId: String,
        status: String,
        completedAt: Long?,
        action: String,
        payload: String? = null,
        actionTakenAt: Long = System.currentTimeMillis()
    ) {
        deliveryRepository.updateTaskStatus(taskId, status, completedAt, updatedAt = actionTakenAt)
        outboxRepository.enqueueTaskAction(
            TaskAction(taskId = taskId, action = action, payload = payload, actionTakenAt = actionTakenAt)
        )
    }
}
