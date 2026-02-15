package com.korbit.deliverytrackingapp.data.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.korbit.deliverytrackingapp.data.local.dao.DeliveryDao
import com.korbit.deliverytrackingapp.data.local.dao.DeliveryDao.TaskWithDeliveryRow
import com.korbit.deliverytrackingapp.data.local.dao.DeliveryTaskDao
import com.korbit.deliverytrackingapp.data.local.dao.TaskActionEventDao
import com.korbit.deliverytrackingapp.data.local.entity.DeliveryEntity
import com.korbit.deliverytrackingapp.data.local.entity.DeliveryTaskEntity
import com.korbit.deliverytrackingapp.data.sync.SyncConfig
import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.model.TaskWithDelivery
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeliveryRepositoryImpl @Inject constructor(
    private val deliveryDao: DeliveryDao,
    private val deliveryTaskDao: DeliveryTaskDao,
    private val taskActionEventDao: TaskActionEventDao,
    private val syncConfig: SyncConfig
) : DeliveryRepository {

    override fun observeAllDeliveries(): Flow<List<Delivery>> =
        deliveryDao.observeAllDeliveriesWithTasks().map { list ->
            list.map { d -> toDomain(d.delivery, d.tasks) }
        }

    override fun observeTasksPaged(pageSize: Int, statusFilter: String): Flow<PagingData<TaskWithDelivery>> =
        androidx.paging.Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = { deliveryDao.getTaskWithDeliveryPagingSource(statusFilter) }
        ).flow.map { pagingData ->
            pagingData.map { row -> toTaskWithDelivery(row) }
        }

    private fun toTaskWithDelivery(row: TaskWithDeliveryRow): TaskWithDelivery =
        TaskWithDelivery(
            task = toDomainTask(row.task),
            delivery = toDomain(row.delivery, listOf(row.task))
        )

    override fun observeDeliveryWithTasks(deliveryId: String): Flow<Delivery?> =
        deliveryDao.observeDeliveryWithTasks(deliveryId).map { it?.let { d -> toDomain(d.delivery, d.tasks) } }

    override suspend fun updateTaskStatus(taskId: String, status: String, completedAt: Long?, updatedAt: Long) {
        val task = deliveryTaskDao.getTaskById(taskId) ?: return
        val wasEverPicked = task.wasEverPicked || (status == "PICKED_UP")
        // After successful pickup, transition task type from PICKUP to DELIVER so it represents the delivery leg.
        val newType = if (status == "PICKED_UP" && task.type.equals("PICKUP", ignoreCase = true)) "DELIVER" else task.type
        val updated = task.copy(type = newType, status = status, completedAt = completedAt, updatedAt = updatedAt, wasEverPicked = wasEverPicked)
        deliveryDao.insertTasks(listOf(updated))
    }

    override suspend fun insertDeliveriesFromSync(deliveries: List<Delivery>) {
        val chunkSize = syncConfig.syncInsertChunkSize
        deliveries.chunked(chunkSize).forEach { chunk ->
            val entities = chunk.map { fromDomain(it).first }
            val tasks = chunk.flatMap { d -> d.tasks.map { fromDomainTask(it, d.id) } }
            deliveryDao.insertAll(entities)
            if (tasks.isNotEmpty()) deliveryDao.insertTasks(tasks)
        }
    }

    override suspend fun updateDeliverySyncedAt(deliveryId: String, syncedAt: Long) {
        deliveryDao.updateSyncedAt(deliveryId, syncedAt)
    }

    override suspend fun insertDeliveryWithTasks(delivery: Delivery) {
        val (entity, taskEntities) = fromDomain(delivery)
        deliveryDao.insert(entity)
        if (taskEntities.isNotEmpty()) deliveryDao.insertTasks(taskEntities)
    }

    override suspend fun getDeliveryById(deliveryId: String): Delivery? =
        deliveryDao.getDeliveryWithTasksOnce(deliveryId)?.let { toDomain(it.delivery, it.tasks) }

    override suspend fun getDeliveryCount(): Int = deliveryDao.getDeliveryCount()

    override suspend fun getTaskCount(statusFilter: String): Int = deliveryDao.getTaskCount(statusFilter)

    override suspend fun clearAllForTesting() {
        taskActionEventDao.deleteAll()
        deliveryDao.deleteAll()
    }

    private fun toDomain(e: DeliveryEntity, tasks: List<DeliveryTaskEntity> = emptyList()): Delivery =
        Delivery(
            id = e.id,
            riderId = e.riderId,
            status = e.status,
            customerName = e.customerName,
            customerAddress = e.customerAddress,
            customerPhone = e.customerPhone,
            warehouseName = e.warehouseName,
            warehouseAddress = e.warehouseAddress,
            lastUpdatedAt = e.lastUpdatedAt,
            syncedAt = e.syncedAt,
            tasks = tasks.map(::toDomainTask)
        )

    private fun toDomainTask(e: DeliveryTaskEntity): DeliveryTask =
        DeliveryTask(
            id = e.id,
            deliveryId = e.deliveryId,
            type = e.type,
            status = e.status,
            sequence = e.sequence,
            completedAt = e.completedAt,
            createdAt = e.createdAt,
            lastModifiedAt = e.updatedAt,
            wasEverPicked = e.wasEverPicked
        )

    private fun fromDomain(d: Delivery): Pair<DeliveryEntity, List<DeliveryTaskEntity>> =
        Pair(
            DeliveryEntity(
                id = d.id,
                riderId = d.riderId,
                status = d.status,
                customerName = d.customerName,
                customerAddress = d.customerAddress,
                customerPhone = d.customerPhone,
                warehouseName = d.warehouseName,
                warehouseAddress = d.warehouseAddress,
                lastUpdatedAt = d.lastUpdatedAt,
                syncedAt = d.syncedAt
            ),
            d.tasks.map { fromDomainTask(it, d.id) }
        )

    private fun fromDomainTask(t: DeliveryTask, deliveryId: String): DeliveryTaskEntity =
        DeliveryTaskEntity(
            id = t.id,
            deliveryId = deliveryId,
            type = t.type,
            status = t.status,
            sequence = t.sequence,
            completedAt = t.completedAt,
            createdAt = t.createdAt,
            updatedAt = t.lastModifiedAt,
            wasEverPicked = t.wasEverPicked
        )
}
