package com.korbit.deliverytrackingapp.domain.model

data class DeliveryTask(
    val id: String,
    val deliveryId: String,
    val type: String,
    val status: String,
    val sequence: Int,
    val completedAt: Long?
)
