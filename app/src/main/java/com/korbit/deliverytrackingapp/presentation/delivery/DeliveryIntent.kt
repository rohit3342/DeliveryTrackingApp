package com.korbit.deliverytrackingapp.presentation.delivery

sealed interface DeliveryIntent {
    data object Load : DeliveryIntent
    data object Refresh : DeliveryIntent
    data class SelectDelivery(val deliveryId: String) : DeliveryIntent
}
