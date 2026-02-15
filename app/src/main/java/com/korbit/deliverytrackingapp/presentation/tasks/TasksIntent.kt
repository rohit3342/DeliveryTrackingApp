package com.korbit.deliverytrackingapp.presentation.tasks

import com.korbit.deliverytrackingapp.domain.model.TaskWithDelivery

sealed interface TasksIntent {
    data object Load : TasksIntent
    data object Refresh : TasksIntent
    data class SetFilter(val filter: TaskFilter) : TasksIntent
    data class OpenTask(val taskWithDelivery: TaskWithDelivery) : TasksIntent
    data object ShowCreatePickup : TasksIntent
    data object DismissCreatePickup : TasksIntent
    data class CreatePickup(val customerName: String, val customerAddress: String, val customerPhone: String) : TasksIntent
    data object ManualSync : TasksIntent
    data object ClearSyncMessage : TasksIntent
}
