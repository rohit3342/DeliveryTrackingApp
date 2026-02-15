package com.korbit.deliverytrackingapp.domain.usecase

import com.korbit.deliverytrackingapp.data.sync.SyncConfig
import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import javax.inject.Inject

/**
 * Inserts a large number of shipments (deliveries + one task each) in chunks.
 * Used for load testing (e.g. 8000 shipments). Uses [SyncConfig.syncInsertChunkSize] per chunk.
 */
class SeedBulkShipmentsUseCase @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val syncConfig: SyncConfig
) {
    /**
     * Inserts [count] deliveries, each with one PICKUP PENDING task.
     * Inserts in chunks to avoid large transactions and memory spikes.
     */
    suspend operator fun invoke(count: Int) {
        val chunkSize = syncConfig.syncInsertChunkSize.coerceAtLeast(1)
        var inserted = 0
        while (inserted < count) {
            val batchSize = minOf(chunkSize, count - inserted)
            val deliveries = (0 until batchSize).map { i ->
                val idx = inserted + i
                val deliveryId = "loadtest_del_$idx"
                val taskId = "loadtest_task_$idx"
                val now = System.currentTimeMillis() - (count - idx) * 1000L
                Delivery(
                    id = deliveryId,
                    riderId = "rider_loadtest",
                    status = "ACTIVE",
                    customerName = "Customer $idx",
                    customerAddress = "Address $idx",
                    customerPhone = "",
                    warehouseName = "Warehouse",
                    warehouseAddress = "Warehouse St",
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
            }
            deliveryRepository.insertDeliveriesFromSync(deliveries)
            inserted += batchSize
        }
    }
}
