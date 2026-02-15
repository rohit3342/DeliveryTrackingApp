package com.korbit.deliverytrackingapp

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.korbit.deliverytrackingapp.core.logging.AppLogger
import com.korbit.deliverytrackingapp.core.monitoring.Monitor
import com.korbit.deliverytrackingapp.data.sync.SyncEngine
import com.korbit.deliverytrackingapp.data.sync.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class DeliveryTrackingApplication : Application() {

    @Inject
    lateinit var syncEngine: SyncEngine

    @Inject
    lateinit var appLogger: AppLogger

    @Inject
    lateinit var monitor: Monitor

    override fun onCreate() {
        super.onCreate()
        scheduleSync()
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "delivery_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
