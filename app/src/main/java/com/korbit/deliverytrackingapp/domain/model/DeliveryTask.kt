package com.korbit.deliverytrackingapp.domain.model

data class DeliveryTask(
    val id: String,
    val deliveryId: String,
    val type: String,
    val status: String,
    val sequence: Int,
    val completedAt: Long?,
    val createdAt: Long = 0L,
    val lastModifiedAt: Long = 0L,
    /** True if this task has ever been in PICKED_UP status (used when FAILED to show Picked vs Reached). */
    val wasEverPicked: Boolean = false
)
