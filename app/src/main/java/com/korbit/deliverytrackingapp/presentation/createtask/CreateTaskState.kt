package com.korbit.deliverytrackingapp.presentation.createtask

data class CreateTaskState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Set to true after create succeeds; screen should navigate back and then clear. */
    val createSuccess: Boolean = false
)
