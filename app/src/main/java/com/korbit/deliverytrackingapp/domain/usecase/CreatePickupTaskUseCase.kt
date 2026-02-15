package com.korbit.deliverytrackingapp.domain.usecase

import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import com.korbit.deliverytrackingapp.domain.repository.OutboxRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Creates a new pickup task offline: writes to Room, enqueues outbox event.
 * Caller should trigger background sync after this.
 */
class CreatePickupTaskUseCase @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val outboxRepository: OutboxRepository
) {
    suspend operator fun invoke(
        customerName: String,
        customerAddress: String,
        customerPhone: String
    ): Pair<Delivery, DeliveryTask> {
        val now = System.currentTimeMillis()
        val deliveryId = "d_${UUID.randomUUID().toString().take(8)}"
        val taskId = "t_${UUID.randomUUID().toString().take(8)}"
        val delivery = Delivery(
            id = deliveryId,
            riderId = "rider_1",
            status = "ACTIVE",
            customerName = customerName,
            customerAddress = customerAddress,
            customerPhone = customerPhone,
            lastUpdatedAt = now,
            syncedAt = null,
            tasks = listOf(
                DeliveryTask(
                    id = taskId,
                    deliveryId = deliveryId,
                    type = "PICKUP",
                    status = "PENDING",
                    sequence = 1,
                    completedAt = null
                )
            )
        )
        deliveryRepository.insertDeliveryWithTasks(delivery)
        outboxRepository.enqueueTaskAction(
            com.korbit.deliverytrackingapp.domain.model.TaskAction(
                taskId = taskId,
                action = "CREATE_PICKUP",
                payload = """{"deliveryId":"$deliveryId","customerName":"$customerName","address":"$customerAddress","phone":"$customerPhone"}"""
            )
        )
        return delivery to delivery.tasks.first()
    }
}
