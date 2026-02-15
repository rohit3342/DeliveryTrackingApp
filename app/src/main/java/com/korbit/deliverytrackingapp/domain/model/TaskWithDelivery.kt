package com.korbit.deliverytrackingapp.domain.model

/**
 * Task with its parent delivery info for "all assigned tasks" list.
 */
data class TaskWithDelivery(
    val task: DeliveryTask,
    val delivery: Delivery
)
