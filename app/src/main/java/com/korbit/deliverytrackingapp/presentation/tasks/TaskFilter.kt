package com.korbit.deliverytrackingapp.presentation.tasks

import androidx.annotation.StringRes
import com.korbit.deliverytrackingapp.R
import com.korbit.deliverytrackingapp.domain.model.TaskWithDelivery

/**
 * Filter pills for the task list. Home UI shows All, Active, Done with counts.
 */
enum class TaskFilter(@StringRes val labelResId: Int, val matches: (TaskWithDelivery) -> Boolean) {
    ALL(R.string.filter_all, { true }),
    ACTIVE(R.string.filter_active, { it.task.status in listOf("PENDING", "PICKED_UP", "REACHED") }),
    DONE(R.string.filter_done, { it.task.status in listOf("DELIVERED", "FAILED") }),
    PENDING(R.string.filter_pending, { it.task.status == "PENDING" }),
    REACHED(R.string.filter_reached, { it.task.status == "REACHED" }),
    PICKED(R.string.filter_picked, { it.task.status == "PICKED_UP" }),
    DELIVERED(R.string.filter_delivered, { it.task.status == "DELIVERED" }),
    FAILED(R.string.filter_failed, { it.task.status == "FAILED" })
}
