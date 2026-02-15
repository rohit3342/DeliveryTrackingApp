package com.korbit.deliverytrackingapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "deliveries",
    indices = [Index(value = ["lastUpdatedAt"])]
)
data class DeliveryEntity(
    @PrimaryKey val id: String,
    val riderId: String,
    val status: String,
    val customerName: String,
    val customerAddress: String,
    val customerPhone: String = "",
    val warehouseName: String = "",
    val warehouseAddress: String = "",
    val lastUpdatedAt: Long,
    val syncedAt: Long?
)
