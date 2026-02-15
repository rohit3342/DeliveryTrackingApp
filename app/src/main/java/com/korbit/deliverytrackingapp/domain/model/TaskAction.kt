package com.korbit.deliverytrackingapp.domain.model

/**
 * Domain representation of a task action to be synced (outbox payload).
 * actionTakenAt: when the delivery agent took the action (for server audit).
 */
data class TaskAction(
    val taskId: String,
    val action: String,
    val payload: String?,
    val actionTakenAt: Long = System.currentTimeMillis()
)
