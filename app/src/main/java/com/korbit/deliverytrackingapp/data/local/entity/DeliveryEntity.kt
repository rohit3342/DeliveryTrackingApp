package com.korbit.deliverytrackingapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deliveries")
data class DeliveryEntity(
    @PrimaryKey val id: String,
    val riderId: String,
    val status: String,
    val customerName: String,
    val customerAddress: String,
    val customerPhone: String = "",
    val warehouseName: String = "",
    val lastUpdatedAt: Long,
    val syncedAt: Long?
)
