package com.korbit.deliverytrackingapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.korbit.deliverytrackingapp.data.local.entity.TaskActionEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskActionEventDao {

    @Query("SELECT COUNT(*) FROM task_action_events WHERE syncStatus = :status")
    suspend fun getPendingCount(status: String = TaskActionEventEntity.SyncStatus.PENDING): Int

    /** Count of events not yet synced (PENDING or FAILED). */
    @Query("SELECT COUNT(*) FROM task_action_events WHERE syncStatus IN ('PENDING', 'FAILED')")
    suspend fun getUnsyncedCount(): Int

    /** Events to send to server: PENDING and FAILED, ordered by createdAt. */
    @Query(
        "SELECT * FROM task_action_events WHERE syncStatus IN ('PENDING', 'FAILED') ORDER BY createdAt ASC LIMIT :limit"
    )
    suspend fun getEventsToSync(limit: Int): List<TaskActionEventEntity>

    @Query(
        "SELECT * FROM task_action_events WHERE syncStatus = :status ORDER BY createdAt ASC LIMIT :limit"
    )
    suspend fun getPendingEvents(
        limit: Int,
        status: String = TaskActionEventEntity.SyncStatus.PENDING
    ): List<TaskActionEventEntity>

    /** Task ids that still need sync (PENDING or FAILED). */
    @Query("SELECT taskId FROM task_action_events WHERE syncStatus IN ('PENDING', 'FAILED')")
    fun observeUnsyncedTaskIds(): Flow<List<String>>

    @Query(
        "UPDATE task_action_events SET syncStatus = :syncedStatus, syncedAt = :syncedAt WHERE id = :eventId"
    )
    suspend fun markSynced(
        eventId: Long,
        syncedAt: Long,
        syncedStatus: String = TaskActionEventEntity.SyncStatus.SYNCED
    )

    @Query("UPDATE task_action_events SET retryCount = retryCount + 1 WHERE id = :eventId")
    suspend fun incrementRetry(eventId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: TaskActionEventEntity): Long

    @Query(
        "UPDATE task_action_events SET syncStatus = :failedStatus, failureReason = :reason WHERE id = :eventId"
    )
    suspend fun markFailed(
        eventId: Long,
        reason: String,
        failedStatus: String = TaskActionEventEntity.SyncStatus.FAILED
    )
}
