package com.sravila.apexmoney.core.network

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object SyncManager {
    private const val SYNC_WORK_NAME = "SupabaseSyncWork"

    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing periodic work if already running
            syncRequest
        )
    }

    fun triggerManualSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "ManualSyncWork",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}
