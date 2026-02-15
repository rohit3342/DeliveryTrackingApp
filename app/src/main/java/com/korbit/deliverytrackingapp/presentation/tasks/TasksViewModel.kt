package com.korbit.deliverytrackingapp.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.korbit.deliverytrackingapp.core.logging.AppLogger
import com.korbit.deliverytrackingapp.domain.usecase.CreatePickupTaskUseCase
import com.korbit.deliverytrackingapp.domain.usecase.EnsureSeedDataUseCase
import com.korbit.deliverytrackingapp.domain.usecase.ObserveAllTasksUseCase
import com.korbit.deliverytrackingapp.domain.usecase.TriggerSyncUseCase
import com.korbit.deliverytrackingapp.domain.usecase.RunFullSyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val observeAllTasksUseCase: ObserveAllTasksUseCase,
    private val ensureSeedDataUseCase: EnsureSeedDataUseCase,
    private val createPickupTaskUseCase: CreatePickupTaskUseCase,
    private val triggerSyncUseCase: TriggerSyncUseCase,
    private val runFullSyncUseCase: RunFullSyncUseCase,
    private val logger: AppLogger
) : ViewModel() {

    private val tag = "TasksViewModel"

    private val _state = MutableStateFlow(TasksState())
    val state: StateFlow<TasksState> = _state.asStateFlow()

    init {
        handle(TasksIntent.Load)
    }

    fun handle(intent: TasksIntent) {
        when (intent) {
            is TasksIntent.Load -> loadTasks()
            is TasksIntent.Refresh -> {
                triggerSyncUseCase()
                loadTasks()
            }
            is TasksIntent.SetFilter -> _state.update { it.copy(selectedFilter = intent.filter) }
            is TasksIntent.OpenTask -> { /* navigation in UI */ }
            is TasksIntent.ShowCreatePickup -> _state.update { it.copy(showCreatePickupDialog = true) }
            is TasksIntent.DismissCreatePickup -> _state.update { it.copy(showCreatePickupDialog = false) }
            is TasksIntent.CreatePickup -> createPickup(intent.customerName, intent.customerAddress, intent.customerPhone)
            is TasksIntent.ManualSync -> runManualSync()
            is TasksIntent.ClearSyncMessage -> _state.update { it.copy(syncMessage = null) }
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            ensureSeedDataUseCase()
            _state.update { it.copy(isLoading = true, error = null) }
            observeAllTasksUseCase()
                .catch { e ->
                    logger.e(tag, "Load tasks failed", e)
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { list ->
                    _state.update {
                        it.copy(tasks = list, isLoading = false, error = null)
                    }
                }
        }
    }

    private fun createPickup(customerName: String, customerAddress: String, customerPhone: String) {
        viewModelScope.launch {
            try {
                createPickupTaskUseCase(customerName, customerAddress, customerPhone)
                triggerSyncUseCase()
                _state.update { it.copy(showCreatePickupDialog = false) }
                logger.d(tag, "Pickup created offline, sync triggered")
            } catch (e: Exception) {
                logger.e(tag, "Create pickup failed", e)
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    private fun runManualSync() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, syncMessage = "Syncing...") }
            runFullSyncUseCase()
                .fold(
                    onSuccess = { result ->
                        _state.update {
                            it.copy(
                                isSyncing = false,
                                syncMessage = "Synced: ${result.outboxSynced} events, ${result.deliveriesSynced} deliveries"
                            )
                        }
                    },
                    onFailure = { e ->
                        logger.e(tag, "Manual sync failed", e)
                        _state.update {
                            it.copy(isSyncing = false, syncMessage = "Sync failed: ${e.message}")
                        }
                    }
                )
        }
    }
}
