package com.korbit.deliverytrackingapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
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
data class TaskEntity(
    @PrimaryKey val id: String,
    val deliveryId: String,
    val type: String,
    val status: String,
    val sequence: Int,
    val completedAt: Long?,
    val version: Int = 0
)
