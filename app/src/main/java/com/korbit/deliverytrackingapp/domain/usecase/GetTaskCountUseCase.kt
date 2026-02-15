package com.korbit.deliverytrackingapp.domain.usecase

import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import javax.inject.Inject

/** Returns task count for a filter (ALL, ACTIVE, DONE) for list header chips. */
class GetTaskCountUseCase @Inject constructor(
    private val deliveryRepository: DeliveryRepository
) {
    suspend operator fun invoke(statusFilter: String): Int =
        deliveryRepository.getTaskCount(statusFilter)
}
