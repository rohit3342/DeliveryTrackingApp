package com.korbit.deliverytrackingapp.presentation.tasks

import androidx.annotation.StringRes
import com.korbit.deliverytrackingapp.R
import com.korbit.deliverytrackingapp.domain.model.TaskWithDelivery

/**
 * Filter pills for the task list. Use labelResId with stringResource() in UI.
 */
enum class TaskFilter(@StringRes val labelResId: Int, val matches: (TaskWithDelivery) -> Boolean) {
    ALL(R.string.filter_all, { true }),
    PENDING(R.string.filter_pending, { it.task.status == "PENDING" }),
    REACHED(R.string.filter_reached, { it.task.status == "REACHED" }),
    PICKED(R.string.filter_picked, { it.task.status == "PICKED_UP" }),
    DELIVERED(R.string.filter_delivered, { it.task.status == "DELIVERED" }),
    FAILED(R.string.filter_failed, { it.task.status == "FAILED" })
}
