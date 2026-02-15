package com.korbit.deliverytrackingapp.presentation.tasks

import com.korbit.deliverytrackingapp.domain.model.TaskWithDelivery

/**
 * Filter pills for the task list.
 */
enum class TaskFilter(val label: String, val matches: (TaskWithDelivery) -> Boolean) {
    ALL("All", { true }),
    PENDING("Pending", { it.task.status == "PENDING" }),
    REACHED("Reached", { it.task.status == "REACHED" }),
    PICKED("Picked", { it.task.status == "PICKED_UP" }),
    DELIVERED("Delivered", { it.task.status == "DELIVERED" }),
    FAILED("Failed delivery", { it.task.status == "FAILED" })
}
