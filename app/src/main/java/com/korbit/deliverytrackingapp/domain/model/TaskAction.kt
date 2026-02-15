package com.korbit.deliverytrackingapp.domain.model

/**
 * Domain representation of a task action to be synced (outbox payload).
 */
data class TaskAction(
    val taskId: String,
    val action: String,
    val payload: String?
)
