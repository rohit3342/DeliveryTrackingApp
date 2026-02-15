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
        warehouseName: String,
        warehouseAddress: String,
        customerName: String,
        customerAddress: String,
        customerPhone: String
    ): Pair<Delivery, DeliveryTask> {
        val now = System.currentTimeMillis()
        val deliveryId = "d_${UUID.randomUUID().toString().take(8)}"
        val taskId = "t_${UUID.randomUUID().toString().take(8)}"
        val orderId = "ORD-${deliveryId.replace("d_", "").uppercase()}"
        val delivery = Delivery(
            id = deliveryId,
            riderId = "rider_1",
            status = "ACTIVE",
            customerName = customerName,
            customerAddress = customerAddress,
            customerPhone = customerPhone,
            warehouseName = warehouseName,
            warehouseAddress = warehouseAddress,
            lastUpdatedAt = now,
            syncedAt = null,
            tasks = listOf(
                DeliveryTask(
                    id = taskId,
                    deliveryId = deliveryId,
                    type = "PICKUP",
                    status = "PENDING",
                    sequence = 1,
                    completedAt = null,
                    createdAt = now,
                    lastModifiedAt = now
                )
            )
        )
        deliveryRepository.insertDeliveryWithTasks(delivery)
        val payload = """{"deliveryId":"$deliveryId","orderId":"$orderId","warehouseName":"$warehouseName","warehouseAddress":"$warehouseAddress","customerName":"$customerName","address":"$customerAddress","phone":"$customerPhone"}"""
        outboxRepository.enqueueTaskAction(
            com.korbit.deliverytrackingapp.domain.model.TaskAction(
                taskId = taskId,
                action = "CREATE_PICKUP",
                payload = payload
            )
        )
        return delivery to delivery.tasks.first()
    }
}
