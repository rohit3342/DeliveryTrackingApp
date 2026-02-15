package com.korbit.deliverytrackingapp.domain.usecase

import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import javax.inject.Inject

/** Inserts seed deliveries/tasks when DB is empty so the app is not blank on first launch. */
class EnsureSeedDataUseCase @Inject constructor(
    private val deliveryRepository: DeliveryRepository
) {
    suspend operator fun invoke() {
        if (deliveryRepository.getDeliveryCount() > 0) return
        val now = System.currentTimeMillis()
        val seed = listOf(
            Delivery(
                id = "seed_1",
                riderId = "rider_1",
                status = "ACTIVE",
                customerName = "Customer A",
                customerAddress = "123 Main St",
                customerPhone = "",
                lastUpdatedAt = now,
                syncedAt = null,
                tasks = listOf(
                    DeliveryTask("t_seed_1a", "seed_1", "PICKUP", "PENDING", 1, null),
                    DeliveryTask("t_seed_1b", "seed_1", "DELIVER", "PENDING", 2, null)
                )
            ),
            Delivery(
                id = "seed_2",
                riderId = "rider_1",
                status = "ACTIVE",
                customerName = "Customer B",
                customerAddress = "456 Oak Ave",
                customerPhone = "",
                lastUpdatedAt = now,
                syncedAt = null,
                tasks = listOf(
                    DeliveryTask("t_seed_2a", "seed_2", "PICKUP", "PENDING", 1, null)
                )
            )
        )
        seed.forEach { deliveryRepository.insertDeliveryWithTasks(it) }
    }
}
