package com.korbit.deliverytrackingapp.domain.repository

import com.korbit.deliverytrackingapp.domain.model.TaskAction
import kotlinx.coroutines.flow.Flow

/**
 * Outbox for task action events. All task actions write to Room + Outbox first.
 * Sync engine reads PENDING events and pushes to API.
 */
interface OutboxRepository {

    suspend fun enqueueTaskAction(action: TaskAction)

    suspend fun getPendingEvents(): List<OutboxEvent>

    suspend fun getPendingCount(): Int

    fun observeUnsyncedTaskIds(): Flow<List<String>>

    suspend fun markSynced(id: Long, syncedAt: Long)

    suspend fun markFailed(id: Long, reason: String)

    data class OutboxEvent(
        val id: Long,
        val taskId: String,
        val action: String,
        val payload: String,
        val actionTakenAt: Long = 0L
    )
}
