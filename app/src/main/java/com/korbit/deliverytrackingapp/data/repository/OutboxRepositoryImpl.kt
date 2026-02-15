package com.korbit.deliverytrackingapp.data.repository

import com.korbit.deliverytrackingapp.data.local.dao.TaskActionEventDao
import com.korbit.deliverytrackingapp.data.local.entity.TaskActionEventEntity
import com.korbit.deliverytrackingapp.domain.model.TaskAction
import com.korbit.deliverytrackingapp.domain.repository.OutboxRepository
import javax.inject.Inject

class OutboxRepositoryImpl @Inject constructor(
    private val taskActionEventDao: TaskActionEventDao
) : OutboxRepository {

    private val defaultPendingLimit = 100

    override suspend fun enqueueTaskAction(action: TaskAction) {
        taskActionEventDao.insert(
            TaskActionEventEntity(
                taskId = action.taskId,
                action = action.action,
                payload = action.payload ?: "",
                actionTakenAt = action.actionTakenAt,
                syncStatus = TaskActionEventEntity.SyncStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun getPendingCount(): Int =
        taskActionEventDao.getPendingCount()

    override suspend fun getPendingEvents(): List<OutboxRepository.OutboxEvent> =
        taskActionEventDao.getPendingEvents(limit = defaultPendingLimit).map { e ->
            OutboxRepository.OutboxEvent(
                id = e.id,
                taskId = e.taskId,
                action = e.action,
                payload = e.payload,
                actionTakenAt = e.actionTakenAt
            )
        }

    override suspend fun markSynced(id: Long, syncedAt: Long) {
        taskActionEventDao.markSynced(eventId = id, syncedAt = syncedAt)
    }

    override suspend fun markFailed(id: Long, reason: String) {
        taskActionEventDao.markFailed(eventId = id, reason = reason)
    }
}
