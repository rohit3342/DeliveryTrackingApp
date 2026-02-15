package com.korbit.deliverytrackingapp.presentation.delivery

import com.korbit.deliverytrackingapp.domain.model.Delivery

data class DeliveryState(
    val deliveries: List<Delivery> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
