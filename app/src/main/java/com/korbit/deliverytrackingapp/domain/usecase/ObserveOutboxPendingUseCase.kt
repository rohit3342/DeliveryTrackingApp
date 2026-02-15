package com.korbit.deliverytrackingapp.domain.usecase

import com.korbit.deliverytrackingapp.domain.repository.OutboxRepository
import javax.inject.Inject

/**
 * Used by SyncEngine only. Returns current list of PENDING outbox events.
 */
class ObserveOutboxPendingUseCase @Inject constructor(
    private val outboxRepository: OutboxRepository
) {
    suspend fun getPending(): List<OutboxRepository.OutboxEvent> =
        outboxRepository.getPendingEvents()
}
