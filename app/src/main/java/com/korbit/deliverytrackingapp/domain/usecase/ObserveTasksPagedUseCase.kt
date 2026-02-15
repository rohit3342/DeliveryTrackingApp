package com.korbit.deliverytrackingapp.domain.usecase

import androidx.paging.PagingData
import com.korbit.deliverytrackingapp.domain.model.TaskWithDelivery
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes tasks with delivery in paged form for scale. statusFilter: ALL, ACTIVE, or DONE. */
class ObserveTasksPagedUseCase @Inject constructor(
    private val deliveryRepository: DeliveryRepository
) {
    operator fun invoke(pageSize: Int, statusFilter: String): Flow<PagingData<TaskWithDelivery>> =
        deliveryRepository.observeTasksPaged(pageSize, statusFilter)
}
