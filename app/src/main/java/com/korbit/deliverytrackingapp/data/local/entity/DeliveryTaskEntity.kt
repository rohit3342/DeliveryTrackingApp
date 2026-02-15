package com.korbit.deliverytrackingapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "delivery_tasks",
    foreignKeys = [
        ForeignKey(
            entity = DeliveryEntity::class,
            parentColumns = ["id"],
            childColumns = ["deliveryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deliveryId")]
)
data class DeliveryTaskEntity(
    @PrimaryKey val id: String,
    val deliveryId: String,
    val type: String,
    val status: String,
    val sequence: Int,
    val completedAt: Long?,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    /** True if this task has ever been in PICKED_UP status. */
    val wasEverPicked: Boolean = false
)
