package com.korbit.deliverytrackingapp.domain.usecase

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.korbit.deliverytrackingapp.data.sync.SYNC_INPUT_FULL_SYNC
import com.korbit.deliverytrackingapp.data.sync.SyncWorker
import javax.inject.Inject

/** Enqueues a one-time sync work when network is available. Outbox-only (no GET /deliveries) to avoid redundant fetch after each action. */
class TriggerSyncUseCase @Inject constructor(
    private val workManager: WorkManager
) {
    operator fun invoke() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(SYNC_INPUT_FULL_SYNC to false))
            .build()
        workManager.enqueue(request)
    }
}
