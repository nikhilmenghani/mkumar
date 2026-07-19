package com.mkumar.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mkumar.backup.worker.DatabaseBackupWorker
import com.mkumar.data.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: PreferencesManager
) {
    companion object {
        private const val PERIODIC_WORK = "mkumar_database_backup_periodic"
        private const val EVENT_WORK = "mkumar_database_backup_event"
        private const val MANUAL_WORK = "mkumar_database_backup_manual"
    }

    fun schedulePeriodic() {
        if (!preferences.backupPrefs.enabled || preferences.githubPrefs.token.isBlank()) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<DatabaseBackupWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints())
            .setInputData(input(BackupTrigger.SCHEDULED))
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun enqueueManual() = enqueue(MANUAL_WORK, BackupTrigger.MANUAL, 0)

    fun enqueueOrderCompleted() {
        if (preferences.backupPrefs.backupOnOrderCompleted && preferences.githubPrefs.token.isNotBlank()) {
            enqueue(EVENT_WORK, BackupTrigger.ORDER_COMPLETED, 45)
        }
    }

    private fun enqueue(name: String, trigger: BackupTrigger, delaySeconds: Long) {
        val request = OneTimeWorkRequestBuilder<DatabaseBackupWorker>()
            .setConstraints(constraints())
            .setInputData(input(trigger))
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            name,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun constraints() = Constraints.Builder()
        .setRequiredNetworkType(
            if (preferences.backupPrefs.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
        )
        .setRequiresBatteryNotLow(true)
        .build()

    private fun input(trigger: BackupTrigger) = Data.Builder()
        .putString(DatabaseBackupWorker.TRIGGER_KEY, trigger.name)
        .build()
}
