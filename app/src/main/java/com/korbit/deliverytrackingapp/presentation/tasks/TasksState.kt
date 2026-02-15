package com.korbit.deliverytrackingapp.presentation.tasks

data class TasksState(
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val doneCount: Int = 0,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val error: String? = null,
    val pendingSyncCount: Int = 0,
    /** Task IDs that have pending sync (bounded set for "pending" pill on cards). */
    val pendingSyncTaskIds: Set<String> = emptySet()
) {
    val isAllSynced: Boolean get() = pendingSyncCount == 0
}
