package com.korbit.deliverytrackingapp.presentation.tasks

import com.korbit.deliverytrackingapp.domain.model.TaskWithDelivery

data class TasksState(
    val tasks: List<TaskWithDelivery> = emptyList(),
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val error: String? = null,
    val showCreatePickupDialog: Boolean = false,
    val pendingSyncCount: Int = 0
) {
    val isAllSynced: Boolean get() = pendingSyncCount == 0
    val filteredTasks: List<TaskWithDelivery> =
        if (selectedFilter == TaskFilter.ALL) tasks else tasks.filter(selectedFilter.matches)
}
