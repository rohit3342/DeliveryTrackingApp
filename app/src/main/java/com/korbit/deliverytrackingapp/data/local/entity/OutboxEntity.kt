package com.korbit.deliverytrackingapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Outbox table for task action events. All writes that need to be synced
 * insert here with status PENDING. Sync engine reads PENDING and pushes to API.
 */
@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: String,
    val action: String,
    val payload: String,
    val status: String = OutboxStatus.PENDING,
    val createdAt: Long,
    val syncedAt: Long? = null,
    val failureReason: String? = null
) {
    object OutboxStatus {
        const val PENDING = "PENDING"
        const val SYNCED = "SYNCED"
        const val FAILED = "FAILED"
    }
}
