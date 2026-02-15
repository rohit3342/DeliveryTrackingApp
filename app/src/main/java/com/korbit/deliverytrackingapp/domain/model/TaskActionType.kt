package com.korbit.deliverytrackingapp.domain.model

/**
 * Rider actions for a task. Each writes to DB + Outbox and triggers sync.
 */
enum class TaskActionType(val value: String) {
    REACHED("REACHED"),
    PICKED_UP("PICKED_UP"),
    DELIVERED("DELIVERED"),
    FAILED("FAILED")
}
