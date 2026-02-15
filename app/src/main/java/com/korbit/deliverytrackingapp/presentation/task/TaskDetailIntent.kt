package com.korbit.deliverytrackingapp.presentation.task

import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.model.TaskActionType

sealed interface TaskDetailIntent {
    data object Load : TaskDetailIntent
    data class PerformAction(val task: DeliveryTask, val action: TaskActionType) : TaskDetailIntent
}
