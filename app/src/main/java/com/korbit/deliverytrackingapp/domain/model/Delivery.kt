package com.korbit.deliverytrackingapp.domain.model

data class Delivery(
    val id: String,
    val riderId: String,
    val status: String,
    val customerName: String,
    val customerAddress: String,
    val customerPhone: String = "",
    val warehouseName: String = "",
    val lastUpdatedAt: Long,
    val syncedAt: Long?,
    val tasks: List<DeliveryTask> = emptyList()
)
