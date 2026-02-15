package com.korbit.deliverytrackingapp.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.korbit.deliverytrackingapp.core.logging.AppLogger
import com.korbit.deliverytrackingapp.core.monitoring.Monitor
import com.korbit.deliverytrackingapp.domain.model.TaskWithDelivery
import com.korbit.deliverytrackingapp.domain.repository.OutboxRepository
import com.korbit.deliverytrackingapp.domain.usecase.EnsureSeedDataUseCase
import com.korbit.deliverytrackingapp.domain.usecase.GetTaskCountUseCase
import com.korbit.deliverytrackingapp.domain.usecase.ObserveTasksPagedUseCase
import com.korbit.deliverytrackingapp.domain.usecase.TriggerSyncUseCase
import com.korbit.deliverytrackingapp.domain.usecase.RunFullSyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 30

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val observeTasksPagedUseCase: ObserveTasksPagedUseCase,
    private val getTaskCountUseCase: GetTaskCountUseCase,
    private val ensureSeedDataUseCase: EnsureSeedDataUseCase,
    private val triggerSyncUseCase: TriggerSyncUseCase,
    private val runFullSyncUseCase: RunFullSyncUseCase,
    private val outboxRepository: OutboxRepository,
    private val logger: AppLogger,
    private val monitor: Monitor
) : ViewModel() {

    private val tag = "TasksViewModel"

    companion object {
        private const val COMPONENT = "tasks_view_model"
    }

    private val _state = MutableStateFlow(TasksState())
    val state: StateFlow<TasksState> = _state.asStateFlow()

    /** Paged task list; recomputes when selectedFilter changes. */
    val pagedTasksFlow: kotlinx.coroutines.flow.Flow<PagingData<TaskWithDelivery>> =
        _state.map { it.selectedFilter }.distinctUntilChanged().flatMapLatest { filter ->
            observeTasksPagedUseCase(PAGE_SIZE, filter.name)
        }

    init {
        handle(TasksIntent.Load)

        viewModelScope.launch {
            outboxRepository.observeUnsyncedTaskIds().collectLatest {
                refreshPendingSyncState()
            }
        }
    }

    fun handle(intent: TasksIntent) {
        when (intent) {
            is TasksIntent.Load -> loadTasks()
            is TasksIntent.Refresh -> {
                triggerSyncUseCase()
                loadTasks()
            }
            is TasksIntent.SetFilter -> {
                monitor.recordEvent(COMPONENT, "filter_changed", mapOf("filter" to intent.filter.name))
                _state.update { it.copy(selectedFilter = intent.filter) }
            }
            is TasksIntent.OpenTask -> { /* navigation in UI */ }
            is TasksIntent.ManualSync -> runManualSync()
            is TasksIntent.ClearSyncMessage -> _state.update { it.copy(syncMessage = null) }
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            ensureSeedDataUseCase()
            _state.update { it.copy(isLoading = true, error = null) }
            monitor.recordEvent(COMPONENT, "load_started", emptyMap())
            try {
                val totalCount = getTaskCountUseCase(TaskFilter.ALL.name)
                val activeCount = getTaskCountUseCase(TaskFilter.ACTIVE.name)
                val doneCount = getTaskCountUseCase(TaskFilter.DONE.name)
                refreshPendingSyncState()
                monitor.recordEvent(COMPONENT, "load_success", mapOf("total_count" to totalCount))
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        totalCount = totalCount,
                        activeCount = activeCount,
                        doneCount = doneCount
                    )
                }
            } catch (e: Exception) {
                logger.e(tag, "Load tasks failed", e)
                monitor.recordEvent(COMPONENT, "load_failed", mapOf("error" to (e.message ?: e.javaClass.simpleName)))
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun refreshPendingSyncState() {
        val pending = outboxRepository.getPendingCount()
        val pendingIds = outboxRepository.getPendingTaskIdsLimit(500)
        _state.update { it.copy(pendingSyncCount = pending, pendingSyncTaskIds = pendingIds) }
    }

    private fun runManualSync() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, syncMessage = "Syncing...") }
            monitor.recordEvent(COMPONENT, "manual_sync_started", emptyMap())
            runFullSyncUseCase()
                .fold(
                    onSuccess = { result ->
                        monitor.recordEvent(COMPONENT, "manual_sync_completed", mapOf("outbox_synced" to result.outboxSynced, "deliveries_synced" to result.deliveriesSynced))
                        refreshPendingSyncState()
                        _state.update {
                            it.copy(
                                isSyncing = false,
                                syncMessage = "Synced: ${result.outboxSynced} events, ${result.deliveriesSynced} deliveries"
                            )
                        }
                    },
                    onFailure = { e ->
                        logger.e(tag, "Manual sync failed", e)
                        monitor.recordEvent(COMPONENT, "manual_sync_failed", mapOf("error" to (e.message ?: e.javaClass.simpleName)))
                        refreshPendingSyncState()
                        _state.update {
                            it.copy(isSyncing = false, syncMessage = "Sync failed: ${e.message}")
                        }
                    }
                )
        }
    }
}
