package com.korbit.deliverytrackingapp.data.sync

import com.korbit.deliverytrackingapp.core.logging.AppLogger
import com.korbit.deliverytrackingapp.data.remote.api.DeliveryApi
import com.korbit.deliverytrackingapp.data.remote.dto.DeliveryDto
import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import javax.inject.Inject

/**
 * Sync engine: (1) Push PENDING outbox events via SyncOrchestrator, (2) Pull deliveries from API into Room.
 * All network access is centralized here – no UI → network.
 */
class SyncEngine @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val syncOrchestrator: SyncOrchestrator,
    private val api: DeliveryApi,
    private val logger: AppLogger
) {
    private val tag = "SyncEngine"

    suspend fun sync(): Result<SyncResult> = runCatching {
        logger.i(tag, "Sync started")
        // 1. Process outbox via orchestrator (max 50, batch send, backoff, partial failure)
        val outboxResult = syncOrchestrator.syncPendingEvents()
        val outboxSynced = outboxResult.getOrElse { SyncOrchestrator.SyncOrchestratorResult(0, 0, 0) }.synced

        // 2. Pull deliveries from API and write to Room
        val apiResult = api.getDeliveries()
        if (!apiResult.isSuccessful) {
            logger.w(tag, "Fetch deliveries failed: ${apiResult.code()}")
            return@runCatching SyncResult(outboxSynced = outboxSynced, deliveriesSynced = 0)
        }
        val dtos = apiResult.body() ?: emptyList()
        val deliveries = dtos.map(::toDomain)
        deliveryRepository.insertDeliveriesFromSync(deliveries)
        logger.i(tag, "Synced ${deliveries.size} deliveries, $outboxSynced outbox events")
        SyncResult(outboxSynced = outboxSynced, deliveriesSynced = deliveries.size)
    }.onFailure {
        logger.e(tag, "Sync failed", it)
    }.map { it ?: SyncResult(0, 0) }

    private fun toDomain(dto: DeliveryDto): Delivery =
        Delivery(
            id = dto.id,
            riderId = dto.riderId,
            status = dto.status,
            customerName = dto.customerName,
            customerAddress = dto.customerAddress,
            customerPhone = dto.customerPhone.orEmpty(),
            warehouseName = dto.warehouseName.orEmpty(),
            warehouseAddress = dto.warehouseAddress.orEmpty(),
            lastUpdatedAt = dto.lastUpdatedAt,
            syncedAt = dto.lastUpdatedAt,
            tasks = (dto.tasks ?: emptyList()).map { t ->
                val wasEverPicked = t.status in listOf("PICKED_UP", "REACHED", "DELIVERED")
                DeliveryTask(
                    id = t.id,
                    deliveryId = dto.id,
                    type = t.type,
                    status = t.status,
                    sequence = t.sequence,
                    completedAt = t.completedAt,
                    createdAt = t.createdAt ?: dto.lastUpdatedAt,
                    lastModifiedAt = t.lastModifiedAt ?: dto.lastUpdatedAt,
                    wasEverPicked = wasEverPicked
                )
            }
        )

    data class SyncResult(val outboxSynced: Int, val deliveriesSynced: Int)
}
