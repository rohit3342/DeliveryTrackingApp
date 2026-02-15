package com.korbit.deliverytrackingapp.domain.usecase

import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDeliveryWithTasksUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    operator fun invoke(deliveryId: String): Flow<Delivery?> =
        repository.observeDeliveryWithTasks(deliveryId)
}
