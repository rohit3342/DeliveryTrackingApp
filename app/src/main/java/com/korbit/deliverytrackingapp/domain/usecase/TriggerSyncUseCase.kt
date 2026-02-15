package com.korbit.deliverytrackingapp.domain.usecase

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.korbit.deliverytrackingapp.data.sync.SyncWorker
import javax.inject.Inject

/** Enqueues a one-time sync work when network is available. Runs when device comes online. */
class TriggerSyncUseCase @Inject constructor(
    private val workManager: WorkManager
) {
    operator fun invoke() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        workManager.enqueue(request)
    }
}
