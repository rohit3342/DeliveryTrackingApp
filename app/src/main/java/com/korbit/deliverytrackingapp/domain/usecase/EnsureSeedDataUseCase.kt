package com.korbit.deliverytrackingapp.domain.usecase

import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import javax.inject.Inject

/** Inserts seed deliveries/tasks when DB is empty. Covers all task statuses for quick testing. */
class EnsureSeedDataUseCase @Inject constructor(
    private val deliveryRepository: DeliveryRepository
) {
    suspend operator fun invoke() {
        if (deliveryRepository.getDeliveryCount() > 0) return
        val now = System.currentTimeMillis()
        val oneHour = 3600_000L
        val seed = listOf(
            // PENDING (Assigned)
            Delivery(
                id = "seed_1",
                riderId = "rider_1",
                status = "ACTIVE",
                customerName = "James Wilson",
                customerAddress = "42 Oak Lane, Brooklyn, NY 11201",
                customerPhone = "+1 (212) 555-0147",
                warehouseName = "Main Warehouse",
                lastUpdatedAt = now,
                syncedAt = null,
                tasks = listOf(
                    DeliveryTask("t_seed_1a", "seed_1", "PICKUP", "PENDING", 1, null, now, now)
                )
            ),
            // PICKED_UP (Picked)
            Delivery(
                id = "seed_2",
                riderId = "rider_1",
                status = "ACTIVE",
                customerName = "Priya Sharma",
                customerAddress = "1580 Commerce Drive, San Jose, CA 95131",
                customerPhone = "+1 (408) 555-0192",
                warehouseName = "Main Warehouse",
                lastUpdatedAt = now - oneHour,
                syncedAt = null,
                tasks = listOf(
                    DeliveryTask("t_seed_2a", "seed_2", "PICKUP", "PICKED_UP", 1, now - oneHour, now - 2 * oneHour, now - oneHour, wasEverPicked = true)
                )
            ),
            // REACHED
            Delivery(
                id = "seed_3",
                riderId = "rider_1",
                status = "ACTIVE",
                customerName = "Marcus Johnson",
                customerAddress = "901 West Peachtree St, Atlanta, GA 30309",
                customerPhone = "+1 (404) 555-0234",
                warehouseName = "North Warehouse",
                lastUpdatedAt = now - 2 * oneHour,
                syncedAt = null,
                tasks = listOf(
                    DeliveryTask("t_seed_3a", "seed_3", "DELIVER", "REACHED", 1, null, now - 3 * oneHour, now - 2 * oneHour, wasEverPicked = true)
                )
            ),
            // DELIVERED
            Delivery(
                id = "seed_4",
                riderId = "rider_1",
                status = "ACTIVE",
                customerName = "Elena Rodriguez",
                customerAddress = "2200 N Loop West, Houston, TX 77018",
                customerPhone = "+1 (713) 555-0456",
                warehouseName = "Main Warehouse",
                lastUpdatedAt = now - 3 * oneHour,
                syncedAt = null,
                tasks = listOf(
                    DeliveryTask("t_seed_4a", "seed_4", "DELIVER", "DELIVERED", 1, now - 3 * oneHour, now - 4 * oneHour, now - 3 * oneHour, wasEverPicked = true)
                )
            ),
            // FAILED, never picked – only "Picked" button
            Delivery(
                id = "seed_5",
                riderId = "rider_1",
                status = "ACTIVE",
                customerName = "David Kim",
                customerAddress = "5500 South Marginal Way, Seattle, WA 98134",
                customerPhone = "+1 (206) 555-0678",
                warehouseName = "West Warehouse",
                lastUpdatedAt = now - 4 * oneHour,
                syncedAt = null,
                tasks = listOf(
                    DeliveryTask("t_seed_5a", "seed_5", "DELIVER", "FAILED", 1, null, now - 5 * oneHour, now - 4 * oneHour)
                )
            ),
            // FAILED, was picked before – only "Reached" button
            Delivery(
                id = "seed_7",
                riderId = "rider_1",
                status = "ACTIVE",
                customerName = "Omar Hassan",
                customerAddress = "3300 S Las Vegas Blvd, Las Vegas, NV 89109",
                customerPhone = "+1 (702) 555-0890",
                warehouseName = "South Warehouse",
                lastUpdatedAt = now - 6 * oneHour,
                syncedAt = null,
                tasks = listOf(
                    DeliveryTask("t_seed_7a", "seed_7", "DELIVER", "FAILED", 1, null, now - 6 * oneHour, now - 6 * oneHour, wasEverPicked = true)
                )
            ),
            // Extra PENDING
            Delivery(
                id = "seed_6",
                riderId = "rider_1",
                status = "ACTIVE",
                customerName = "Sophie Chen",
                customerAddress = "100 Market St, San Francisco, CA 94105",
                customerPhone = "+1 (415) 555-0321",
                warehouseName = "Main Warehouse",
                lastUpdatedAt = now - 5 * oneHour,
                syncedAt = null,
                tasks = listOf(
                    DeliveryTask("t_seed_6a", "seed_6", "PICKUP", "PENDING", 1, null, now - 5 * oneHour, now - 5 * oneHour)
                )
            )
        )
        seed.forEach { deliveryRepository.insertDeliveryWithTasks(it) }
    }
}
