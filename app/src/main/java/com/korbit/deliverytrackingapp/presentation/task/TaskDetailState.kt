package com.korbit.deliverytrackingapp.presentation.task

import com.korbit.deliverytrackingapp.domain.model.Delivery

data class TaskDetailState(
    val delivery: Delivery? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
