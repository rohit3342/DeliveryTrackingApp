package com.korbit.deliverytrackingapp.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.korbit.deliverytrackingapp.DeliveryTrackingApplication
import com.korbit.deliverytrackingapp.core.logging.AppLogger

/**
 * WorkManager worker: runs SyncEngine in background. No direct UI → network.
 * Uses standard (Context, WorkerParameters) constructor so WorkManager can create it by reflection;
 * dependencies are obtained from the Application.
 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val app = appContext.applicationContext as DeliveryTrackingApplication
    private val syncEngine: SyncEngine get() = app.syncEngine
    private val logger: AppLogger get() = app.appLogger

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
