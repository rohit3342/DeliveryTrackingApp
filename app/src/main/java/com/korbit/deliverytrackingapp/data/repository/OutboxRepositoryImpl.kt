package com.korbit.deliverytrackingapp.data.repository

import com.korbit.deliverytrackingapp.data.local.dao.OutboxDao
import com.korbit.deliverytrackingapp.data.local.entity.OutboxEntity
import com.korbit.deliverytrackingapp.domain.model.TaskAction
import com.korbit.deliverytrackingapp.domain.repository.OutboxRepository
import javax.inject.Inject

class OutboxRepositoryImpl @Inject constructor(
    private val outboxDao: OutboxDao
) : OutboxRepository {

    override suspend fun enqueueTaskAction(action: TaskAction) {
        outboxDao.insert(
            OutboxEntity(
                taskId = action.taskId,
                action = action.action,
                payload = action.payload ?: "",
                status = OutboxEntity.OutboxStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun getPendingEvents(): List<OutboxRepository.OutboxEvent> =
        outboxDao.getPendingEvents().map { e ->
            OutboxRepository.OutboxEvent(
                id = e.id,
                taskId = e.taskId,
                action = e.action,
                payload = e.payload
            )
        }

    override suspend fun markSynced(id: Long, syncedAt: Long) {
        outboxDao.updateStatus(id, OutboxEntity.OutboxStatus.SYNCED, syncedAt, null)
    }

    override suspend fun markFailed(id: Long, reason: String) {
        outboxDao.updateStatus(id, OutboxEntity.OutboxStatus.FAILED, null, reason)
    }
}
