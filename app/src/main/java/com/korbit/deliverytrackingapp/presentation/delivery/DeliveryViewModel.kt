package com.korbit.deliverytrackingapp.presentation.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.korbit.deliverytrackingapp.core.logging.AppLogger
import com.korbit.deliverytrackingapp.domain.usecase.GetDeliveriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeliveryViewModel @Inject constructor(
    private val getDeliveriesUseCase: GetDeliveriesUseCase,
    private val logger: AppLogger
) : ViewModel() {

    private val tag = "DeliveryViewModel"

    private val _state = MutableStateFlow(DeliveryState())
    val state: StateFlow<DeliveryState> = _state.asStateFlow()

    init {
        handle(DeliveryIntent.Load)
    }

    fun handle(intent: DeliveryIntent) {
        when (intent) {
            is DeliveryIntent.Load -> loadDeliveries()
            is DeliveryIntent.Refresh -> loadDeliveries()
            is DeliveryIntent.SelectDelivery -> { /* navigation handled in UI */ }
        }
    }

    private fun loadDeliveries() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getDeliveriesUseCase()
                .catch { e ->
                    logger.e(tag, "Load deliveries failed", e)
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { list ->
                    _state.update {
                        it.copy(deliveries = list, isLoading = false, error = null)
                    }
                }
        }
    }
}
