package com.korbit.deliverytrackingapp.presentation.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.korbit.deliverytrackingapp.core.logging.AppLogger
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.model.TaskActionType
import com.korbit.deliverytrackingapp.domain.usecase.GetDeliveryWithTasksUseCase
import com.korbit.deliverytrackingapp.domain.usecase.TriggerSyncUseCase
import com.korbit.deliverytrackingapp.domain.usecase.UpdateTaskStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDeliveryWithTasksUseCase: GetDeliveryWithTasksUseCase,
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase,
    private val triggerSyncUseCase: TriggerSyncUseCase,
    private val logger: AppLogger
) : ViewModel() {

    private val tag = "TaskDetailViewModel"
    private val deliveryId: String = checkNotNull(savedStateHandle["deliveryId"])

    private val _state = MutableStateFlow(TaskDetailState())
    val state: StateFlow<TaskDetailState> = _state.asStateFlow()

    init {
        handle(TaskDetailIntent.Load)
    }

    fun handle(intent: TaskDetailIntent) {
        when (intent) {
            is TaskDetailIntent.Load -> loadDelivery()
            is TaskDetailIntent.PerformAction -> performAction(intent.task, intent.action)
        }
    }

    private fun loadDelivery() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getDeliveryWithTasksUseCase(deliveryId)
                .catch { e ->
                    logger.e(tag, "Load delivery failed", e)
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { delivery ->
                    _state.update {
                        it.copy(delivery = delivery, isLoading = false, error = null)
                    }
                }
        }
    }

    private fun performAction(task: DeliveryTask, action: TaskActionType) {
        viewModelScope.launch {
            try {
                val completedAt = when (action) {
                    TaskActionType.REACHED, TaskActionType.PICKED_UP,
                    TaskActionType.DELIVERED, TaskActionType.FAILED -> System.currentTimeMillis()
                }
                updateTaskStatusUseCase(
                    taskId = task.id,
                    status = action.value,
                    completedAt = completedAt,
                    action = action.value,
                    payload = null
                )
                triggerSyncUseCase()
                logger.d(tag, "Task ${task.id} action=${action.value} (Room + Outbox, sync triggered)")
            } catch (e: Exception) {
                logger.e(tag, "Perform action failed", e)
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}
