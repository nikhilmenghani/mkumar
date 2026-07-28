package com.mkumar.backup.worker

import android.content.Context
import android.content.pm.ServiceInfo
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mkumar.backup.BackupCoordinator
import com.mkumar.backup.BackupResult
import com.mkumar.backup.BackupTrigger
import com.mkumar.data.PreferencesManager
import com.mkumar.notification.NotificationUtility
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltWorker
class DatabaseBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val coordinator: BackupCoordinator,
    private val preferences: PreferencesManager
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = BACKUP_MUTEX.withLock {
        val trigger = inputData.getString(TRIGGER_KEY)
            ?.let { runCatching { BackupTrigger.valueOf(it) }.getOrNull() }
            ?: BackupTrigger.SCHEDULED
        if (!preferences.backupPrefs.enabled) {
            return@withLock Result.success()
        }
        if (trigger == BackupTrigger.SCHEDULED && !scheduledBackupIsDue()) {
            return@withLock Result.success(
                workDataOf(PROGRESS_STAGE_KEY to "Backup is not due yet")
            )
        }
        setForeground(backupForegroundInfo("Preparing database backup", 0))
        when (val result = coordinator.backup(trigger) { stage, percent ->
            setProgress(workDataOf(PROGRESS_STAGE_KEY to stage, PROGRESS_PERCENT_KEY to percent))
            NotificationUtility.updateProgress(
                applicationContext,
                NotificationUtility.BACKUP_NOTIFICATION_ID,
                "Database backup",
                stage,
                percent
            )
        }) {
            is BackupResult.Success -> Result.success(
                workDataOf(PROGRESS_STAGE_KEY to "Backup completed", PROGRESS_PERCENT_KEY to 100)
            )
            is BackupResult.Failure -> when {
                // A periodic request must remain healthy for its next interval. Treat this
                // attempt as complete instead of retrying it several times within minutes.
                trigger == BackupTrigger.SCHEDULED -> Result.success(
                    workDataOf(ERROR_MESSAGE_KEY to result.message)
                )
                runAttemptCount < 3 -> Result.retry()
                else -> Result.failure(workDataOf(ERROR_MESSAGE_KEY to result.message))
            }
        }
    }

    private fun scheduledBackupIsDue(): Boolean {
        val intervalHours = preferences.backupPrefs.intervalHours
        if (intervalHours <= 0 || preferences.githubPrefs.token.isBlank()) return false
        val lastSuccessful = preferences.backupPrefs.lastSuccessfulBackupAt
            .takeIf(String::isNotBlank)
            ?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
            ?: return true
        return System.currentTimeMillis() - lastSuccessful >=
            TimeUnit.HOURS.toMillis(intervalHours.toLong())
    }

    private fun backupForegroundInfo(stage: String, percent: Int): ForegroundInfo =
        ForegroundInfo(
            NotificationUtility.BACKUP_NOTIFICATION_ID,
            NotificationUtility.progressNotification(
                applicationContext,
                NotificationUtility.BACKUP_NOTIFICATION_ID,
                "Database backup",
                stage,
                percent
            ),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )

    companion object {
        private val BACKUP_MUTEX = Mutex()
        const val TRIGGER_KEY = "backup_trigger"
        const val PROGRESS_STAGE_KEY = "backup_progress_stage"
        const val PROGRESS_PERCENT_KEY = "backup_progress_percent"
        const val ERROR_MESSAGE_KEY = "backup_error_message"
    }
}
