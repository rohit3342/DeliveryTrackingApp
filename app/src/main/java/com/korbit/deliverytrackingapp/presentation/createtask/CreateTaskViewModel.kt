package com.korbit.deliverytrackingapp.presentation.createtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.korbit.deliverytrackingapp.core.logging.AppLogger
import com.korbit.deliverytrackingapp.core.monitoring.Monitor
import com.korbit.deliverytrackingapp.domain.usecase.CreatePickupTaskUseCase
import com.korbit.deliverytrackingapp.domain.usecase.TriggerSyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val createPickupTaskUseCase: CreatePickupTaskUseCase,
    private val triggerSyncUseCase: TriggerSyncUseCase,
    private val logger: AppLogger,
    private val monitor: Monitor
) : ViewModel() {

    private val tag = "CreateTaskViewModel"

    companion object {
        private const val COMPONENT = "create_task"
    }

    private val _state = MutableStateFlow(CreateTaskState())
    val state: StateFlow<CreateTaskState> = _state.asStateFlow()

    fun create(
        warehouseName: String,
        warehouseAddress: String,
        customerName: String,
        customerAddress: String,
        customerPhone: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            monitor.recordEvent(COMPONENT, "create_started", emptyMap())
            try {
                createPickupTaskUseCase(
                    warehouseName,
                    warehouseAddress,
                    customerName,
                    customerAddress,
                    customerPhone
                )
                triggerSyncUseCase()
                monitor.recordEvent(COMPONENT, "create_success", emptyMap())
                _state.update { it.copy(isLoading = false, createSuccess = true) }
                logger.d(tag, "Create task success, navigate back")
            } catch (e: Exception) {
                logger.e(tag, "Create task failed", e)
                monitor.recordEvent(COMPONENT, "create_failed", mapOf("error" to (e.message ?: e.javaClass.simpleName)))
                _state.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun clearCreateSuccess() {
        _state.update { it.copy(createSuccess = false) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
