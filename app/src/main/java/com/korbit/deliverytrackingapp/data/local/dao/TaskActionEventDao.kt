package com.korbit.deliverytrackingapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.korbit.deliverytrackingapp.data.local.entity.TaskActionEventEntity

@Dao
interface TaskActionEventDao {

    @Query(
        "SELECT * FROM task_action_events WHERE syncStatus = :status ORDER BY createdAt ASC LIMIT :limit"
    )
    suspend fun getPendingEvents(
        limit: Int,
        status: String = TaskActionEventEntity.SyncStatus.PENDING
    ): List<TaskActionEventEntity>

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
