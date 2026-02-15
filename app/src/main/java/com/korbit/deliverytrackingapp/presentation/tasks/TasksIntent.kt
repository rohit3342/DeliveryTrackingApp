package com.korbit.deliverytrackingapp.presentation.tasks

import com.korbit.deliverytrackingapp.domain.model.TaskWithDelivery

sealed interface TasksIntent {
    data object Load : TasksIntent
    data object Refresh : TasksIntent
    data class SetFilter(val filter: TaskFilter) : TasksIntent
    data class OpenTask(val taskWithDelivery: TaskWithDelivery) : TasksIntent
    data object ManualSync : TasksIntent
    data object ClearSyncMessage : TasksIntent
}
