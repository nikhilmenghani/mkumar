package com.mkumar.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.BackoffPolicy
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
import java.util.UUID
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
        private const val ORDER_COMPLETED_BACKUP_ENABLED = false
        const val BACKUP_WORK_TAG = "mkumar_database_backup"
        const val MANUAL_WORK_TAG = "mkumar_database_backup_manual_tag"
        const val SCHEDULED_WORK_TAG = "mkumar_database_backup_scheduled_tag"
        const val EVENT_WORK_TAG = "mkumar_database_backup_event_tag"
    }

    fun schedulePeriodic(updateExisting: Boolean = false) {
        clearObsoleteFtsValidationError()
        val intervalHours = preferences.backupPrefs.intervalHours
        if (!preferences.backupPrefs.enabled || intervalHours <= 0 || preferences.githubPrefs.token.isBlank()) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<DatabaseBackupWorker>(intervalHours.toLong(), TimeUnit.HOURS)
            .setConstraints(constraints())
            .setInputData(input(BackupTrigger.SCHEDULED))
            .setInitialDelay(intervalHours.toLong(), TimeUnit.HOURS)
            .addTag(BACKUP_WORK_TAG)
            .addTag(SCHEDULED_WORK_TAG)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            if (updateExisting) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun enqueueManual(): UUID {
        check(preferences.backupPrefs.enabled) { "Database backups are disabled" }
        return enqueue(MANUAL_WORK, BackupTrigger.MANUAL, 0)
    }

    fun cancelAllBackupWork() {
        WorkManager.getInstance(context).cancelAllWorkByTag(BACKUP_WORK_TAG)
    }

    fun enqueueOrderCompleted() {
        if (preferences.backupPrefs.enabled &&
            ORDER_COMPLETED_BACKUP_ENABLED &&
            preferences.backupPrefs.backupOnOrderCompleted &&
            preferences.githubPrefs.token.isNotBlank()
        ) {
            enqueue(EVENT_WORK, BackupTrigger.ORDER_COMPLETED, 45)
        }
    }

    private fun enqueue(name: String, trigger: BackupTrigger, delaySeconds: Long): UUID {
        val request = OneTimeWorkRequestBuilder<DatabaseBackupWorker>()
            .setConstraints(constraints())
            .setInputData(input(trigger))
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(BACKUP_WORK_TAG)
            .addTag(
                when (trigger) {
                    BackupTrigger.MANUAL -> MANUAL_WORK_TAG
                    BackupTrigger.SCHEDULED -> SCHEDULED_WORK_TAG
                    BackupTrigger.ORDER_COMPLETED -> EVENT_WORK_TAG
                }
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            name,
            ExistingWorkPolicy.REPLACE,
            request
        )
        return request.id
    }

    private fun constraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private fun input(trigger: BackupTrigger) = Data.Builder()
        .putString(DatabaseBackupWorker.TRIGGER_KEY, trigger.name)
        .build()

    private fun clearObsoleteFtsValidationError() {
        val error = preferences.backupPrefs.lastBackupError
        if (error.contains("unable to validate the inverted index for FTS4", ignoreCase = true) &&
            error.contains("readonly database", ignoreCase = true)
        ) {
            preferences.backupPrefs.lastBackupError = ""
        }
    }

}
