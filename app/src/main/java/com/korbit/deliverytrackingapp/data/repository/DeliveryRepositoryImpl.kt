package com.korbit.deliverytrackingapp.data.repository

import com.korbit.deliverytrackingapp.data.local.dao.DeliveryDao
import com.korbit.deliverytrackingapp.data.local.dao.DeliveryTaskDao
import com.korbit.deliverytrackingapp.data.local.entity.DeliveryEntity
import com.korbit.deliverytrackingapp.data.local.entity.DeliveryTaskEntity
import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeliveryRepositoryImpl @Inject constructor(
    private val deliveryDao: DeliveryDao,
    private val deliveryTaskDao: DeliveryTaskDao
) : DeliveryRepository {

    override fun observeAllDeliveries(): Flow<List<Delivery>> =
        deliveryDao.observeAllDeliveriesWithTasks().map { list ->
            list.map { d -> toDomain(d.delivery, d.tasks) }
        }

    override fun observeDeliveryWithTasks(deliveryId: String): Flow<Delivery?> =
        deliveryDao.observeDeliveryWithTasks(deliveryId).map { it?.let { d -> toDomain(d.delivery, d.tasks) } }

    override suspend fun updateTaskStatus(taskId: String, status: String, completedAt: Long?) {
        val task = deliveryTaskDao.getTaskById(taskId) ?: return
        val updated = task.copy(status = status, completedAt = completedAt)
        deliveryDao.insertTasks(listOf(updated))
    }

    override suspend fun insertDeliveriesFromSync(deliveries: List<Delivery>) {
        val entities = deliveries.map { fromDomain(it).first }
        val allTasks = deliveries.flatMap { d -> d.tasks.map { fromDomainTask(it, d.id) } }
        deliveryDao.insertAll(entities)
        if (allTasks.isNotEmpty()) deliveryDao.insertTasks(allTasks)
    }

    override suspend fun updateDeliverySyncedAt(deliveryId: String, syncedAt: Long) {
        deliveryDao.updateSyncedAt(deliveryId, syncedAt)
    }

    override suspend fun insertDeliveryWithTasks(delivery: Delivery) {
        val (entity, taskEntities) = fromDomain(delivery)
        deliveryDao.insert(entity)
        if (taskEntities.isNotEmpty()) deliveryDao.insertTasks(taskEntities)
    }

    override suspend fun getDeliveryCount(): Int = deliveryDao.getDeliveryCount()

    private fun toDomain(e: DeliveryEntity, tasks: List<DeliveryTaskEntity> = emptyList()): Delivery =
        Delivery(
            id = e.id,
            riderId = e.riderId,
            status = e.status,
            customerName = e.customerName,
            customerAddress = e.customerAddress,
            customerPhone = e.customerPhone,
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
            completedAt = e.completedAt
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
            completedAt = t.completedAt
        )
}
