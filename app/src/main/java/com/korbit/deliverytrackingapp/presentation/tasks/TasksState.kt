package com.korbit.deliverytrackingapp.presentation.tasks

import com.korbit.deliverytrackingapp.domain.model.TaskWithDelivery

data class TasksState(
    val tasks: List<TaskWithDelivery> = emptyList(),
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val error: String? = null,
    val pendingSyncCount: Int = 0,
    /** Task IDs that have at least one PENDING outbox event (show "pending sync" pill on card). */
    val pendingSyncTaskIds: Set<String> = emptySet()
) {
    val isAllSynced: Boolean get() = pendingSyncCount == 0
    val filteredTasks: List<TaskWithDelivery> =
        if (selectedFilter == TaskFilter.ALL) tasks else tasks.filter(selectedFilter.matches)
}
