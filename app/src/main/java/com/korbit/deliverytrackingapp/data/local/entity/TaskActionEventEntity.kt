package com.korbit.deliverytrackingapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Outbox table for task action events. All writes that need to be synced
 * insert here with syncStatus PENDING. Sync engine reads PENDING and pushes to API.
 */
@Entity(
    tableName = "task_action_events",
    indices = [
        Index("taskId"),
        Index("syncStatus"),
        Index(value = ["syncStatus", "createdAt"])
    ]
)
data class TaskActionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: String,
    val action: String,
    val payload: String,
    val actionTakenAt: Long = 0L,
    val syncStatus: String = SyncStatus.PENDING,
    val createdAt: Long,
    val syncedAt: Long? = null,
    val failureReason: String? = null,
    val retryCount: Int = 0,
    val version: Int = 0
) {
    object SyncStatus {
        const val PENDING = "PENDING"
        const val SYNCED = "SYNCED"
        const val FAILED = "FAILED"
    }
}
