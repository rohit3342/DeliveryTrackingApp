package com.korbit.deliverytrackingapp.presentation.createtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.korbit.deliverytrackingapp.core.logging.AppLogger
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
    private val logger: AppLogger
) : ViewModel() {

    private val tag = "CreateTaskViewModel"

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
            try {
                createPickupTaskUseCase(
                    warehouseName,
                    warehouseAddress,
                    customerName,
                    customerAddress,
                    customerPhone
                )
                triggerSyncUseCase()
                _state.update { it.copy(isLoading = false, createSuccess = true) }
                logger.d(tag, "Create task success, navigate back")
            } catch (e: Exception) {
                logger.e(tag, "Create task failed", e)
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
