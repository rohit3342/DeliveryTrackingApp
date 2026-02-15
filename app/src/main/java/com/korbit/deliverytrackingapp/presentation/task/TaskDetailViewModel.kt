package com.korbit.deliverytrackingapp.presentation.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.korbit.deliverytrackingapp.core.logging.AppLogger
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.usecase.GetDeliveryWithTasksUseCase
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
            is TaskDetailIntent.CompleteTask -> completeTask(intent.task)
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

    private fun completeTask(task: DeliveryTask) {
        viewModelScope.launch {
            try {
                updateTaskStatusUseCase(
                    taskId = task.id,
                    status = "COMPLETED",
                    completedAt = System.currentTimeMillis(),
                    action = "COMPLETE",
                    payload = null
                )
                logger.d(tag, "Task ${task.id} marked completed (written to Room + Outbox)")
            } catch (e: Exception) {
                logger.e(tag, "Complete task failed", e)
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}
