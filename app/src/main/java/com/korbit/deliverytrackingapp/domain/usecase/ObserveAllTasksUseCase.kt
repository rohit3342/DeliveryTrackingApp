package com.korbit.deliverytrackingapp.domain.usecase

import com.korbit.deliverytrackingapp.domain.model.TaskWithDelivery
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Observes all assigned tasks (flattened from deliveries) from Room. */
class ObserveAllTasksUseCase @Inject constructor(
    private val deliveryRepository: DeliveryRepository
) {
    operator fun invoke(): Flow<List<TaskWithDelivery>> =
        deliveryRepository.observeAllDeliveries().map { deliveries ->
            deliveries.flatMap { d ->
                d.tasks.map { t -> TaskWithDelivery(task = t, delivery = d) }
            }.sortedWith(
                compareBy<TaskWithDelivery> { it.delivery.lastUpdatedAt }.reversed()
                    .thenBy { it.task.sequence }
            )
        }
}
