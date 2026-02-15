package com.korbit.deliverytrackingapp.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.korbit.deliverytrackingapp.DeliveryTrackingApplication
import com.korbit.deliverytrackingapp.core.logging.AppLogger

/** Key for InputData: when true, run full sync (outbox + GET /deliveries); when false, outbox only. */
const val SYNC_INPUT_FULL_SYNC = "full_sync"

/**
 * WorkManager worker: runs SyncEngine in background. No direct UI → network.
 * Uses standard (Context, WorkerParameters) constructor so WorkManager can create it by reflection;
 * dependencies are obtained from the Application.
 * Reads InputData "full_sync" (default true): false = outbox-only (e.g. after action), true = full sync.
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
        val fullSync = inputData.getBoolean(SYNC_INPUT_FULL_SYNC, true)
        logger.i(tag, "SyncWorker doWork started (fullSync=$fullSync)")
        return syncEngine.sync(fetchDeliveries = fullSync)
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
