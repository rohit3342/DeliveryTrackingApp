package com.korbit.deliverytrackingapp.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.korbit.deliverytrackingapp.core.logging.AppLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker: runs SyncEngine in background. No direct UI → network.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncEngine: SyncEngine,
    private val logger: AppLogger
) : CoroutineWorker(appContext, params) {

    private val tag = "SyncWorker"

    override suspend fun doWork(): Result {
        logger.i(tag, "SyncWorker doWork started")
        return syncEngine.sync()
            .fold(
                onSuccess = {
                    logger.i(tag, "Sync completed: $it")
                    Result.success()
                },
                onFailure = {
                    logger.e(tag, "Sync failed", it)
                    Result.retry()
                }
            )
    }
}
