package com.mkumar.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mkumar.sync.worker.PullFromCloudWorker
import com.mkumar.sync.worker.SyncOutboxWorker
import java.util.concurrent.TimeUnit

object SyncScheduler {

    private const val PUSH_SYNC_WORK = "mkumar_push_sync"
    private const val PULL_SYNC_WORK = "mkumar_pull_sync"

    private fun networkConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    /**
     * Trigger push sync (Outbox → Cloud).
     * Safe to call many times (deduped by unique work).
     */
    fun enqueuePushSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncOutboxWorker>()
            .setConstraints(networkConstraints())
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                PUSH_SYNC_WORK,
                ExistingWorkPolicy.KEEP,
                request
            )
    }

    /**
     * Periodic pull sync (Cloud → Local).
     * Call once at app startup.
     */
    fun schedulePeriodicPull(context: Context) {
        val request = PeriodicWorkRequestBuilder<PullFromCloudWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(networkConstraints())
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                PULL_SYNC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    /**
     * Manual pull trigger (Settings → Refresh).
     */
    fun triggerManualPull(context: Context) {
        val request = OneTimeWorkRequestBuilder<PullFromCloudWorker>()
            .setConstraints(networkConstraints())
            .build()

        WorkManager.getInstance(context)
            .enqueue(request)
    }
}
