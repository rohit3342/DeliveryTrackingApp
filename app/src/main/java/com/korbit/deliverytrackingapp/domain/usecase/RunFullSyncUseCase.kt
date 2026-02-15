package com.korbit.deliverytrackingapp.domain.usecase

import com.korbit.deliverytrackingapp.data.sync.SyncEngine
import javax.inject.Inject

/**
 * Runs full sync: push all pending outbox events to server and fetch new pickup/delivery tasks.
 * Call from UI for manual sync.
 */
class RunFullSyncUseCase @Inject constructor(
    private val syncEngine: SyncEngine
) {
    suspend operator fun invoke() = syncEngine.sync()
}
