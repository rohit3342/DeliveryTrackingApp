package com.korbit.deliverytrackingapp.data.sync

import com.korbit.deliverytrackingapp.core.logging.AppLogger
import com.korbit.deliverytrackingapp.core.monitoring.Monitor
import com.korbit.deliverytrackingapp.data.remote.api.DeliveryApi
import com.korbit.deliverytrackingapp.data.remote.dto.DeliveryDto
import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import javax.inject.Inject

/**
 * Sync engine: (1) Push PENDING outbox events via SyncOrchestrator, (2) optionally pull deliveries from API into Room.
 * All network access is centralized here – no UI → network.
 *
 * When a task action is performed we only push outbox (no GET /deliveries). Full sync (with GET /deliveries) runs on periodic or manual sync.
 */
class SyncEngine @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val syncOrchestrator: SyncOrchestrator,
    private val api: DeliveryApi,
    private val logger: AppLogger,
    private val monitor: Monitor
) {
    private val tag = "SyncEngine"

    companion object {
        private const val COMPONENT = "sync_engine"
    }

    suspend fun sync(fetchDeliveries: Boolean = true): Result<SyncResult> = runCatching {
        logger.i(tag, "Sync started (fetchDeliveries=$fetchDeliveries)")
        monitor.recordEvent(COMPONENT, "sync_started", mapOf("fetch_deliveries" to fetchDeliveries))
        val outboxResult = syncOrchestrator.syncPendingEvents()
        val outboxSynced = outboxResult.getOrElse { SyncOrchestrator.SyncOrchestratorResult(0, 0, 0) }.synced

        // After task action we skip GET /deliveries; only periodic/manual sync fetches deliveries.
        if (!fetchDeliveries) {
            logger.i(tag, "Outbox-only sync: $outboxSynced events (no GET /deliveries)")
            monitor.recordEvent(COMPONENT, "sync_completed", mapOf("outbox_synced" to outboxSynced, "deliveries_synced" to 0))
            return@runCatching SyncResult(outboxSynced = outboxSynced, deliveriesSynced = 0)
        }

        val apiResult = api.getDeliveries()
        if (!apiResult.isSuccessful) {
            logger.w(tag, "Fetch deliveries failed: ${apiResult.code()}")
            monitor.recordEvent(COMPONENT, "fetch_deliveries_failed", mapOf("code" to apiResult.code(), "outbox_synced" to outboxSynced))
            return@runCatching SyncResult(outboxSynced = outboxSynced, deliveriesSynced = 0)
        }
        val dtos = apiResult.body() ?: emptyList()
        val deliveries = dtos.map(::toDomain)
        deliveryRepository.insertDeliveriesFromSync(deliveries)
        logger.i(tag, "Synced ${deliveries.size} deliveries, $outboxSynced outbox events")
        val result = SyncResult(outboxSynced = outboxSynced, deliveriesSynced = deliveries.size)
        monitor.recordEvent(COMPONENT, "sync_completed", mapOf("outbox_synced" to result.outboxSynced, "deliveries_synced" to result.deliveriesSynced))
        result
    }.onFailure {
        logger.e(tag, "Sync failed", it)
        monitor.recordEvent(COMPONENT, "sync_failed", mapOf("error" to (it.message ?: it.javaClass.simpleName)))
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
                val wasEverPicked = t.type in listOf("DELIVER")
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
