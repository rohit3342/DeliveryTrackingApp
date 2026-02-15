package com.korbit.deliverytrackingapp.domain.usecase

import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes deliveries from Room (single source of truth). No network.
 */
class GetDeliveriesUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    operator fun invoke(): Flow<List<Delivery>> = repository.observeAllDeliveries()
}
