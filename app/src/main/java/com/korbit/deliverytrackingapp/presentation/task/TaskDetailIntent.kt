package com.korbit.deliverytrackingapp.presentation.task

import com.korbit.deliverytrackingapp.domain.model.DeliveryTask

sealed interface TaskDetailIntent {
    data object Load : TaskDetailIntent
    data class CompleteTask(val task: DeliveryTask) : TaskDetailIntent
}
