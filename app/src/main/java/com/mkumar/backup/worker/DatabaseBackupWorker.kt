package com.mkumar.backup.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mkumar.backup.BackupCoordinator
import com.mkumar.backup.BackupResult
import com.mkumar.backup.BackupTrigger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DatabaseBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val coordinator: BackupCoordinator
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val trigger = inputData.getString(TRIGGER_KEY)
            ?.let { runCatching { BackupTrigger.valueOf(it) }.getOrNull() }
            ?: BackupTrigger.SCHEDULED
        return when (val result = coordinator.backup(trigger) { stage, percent ->
            setProgress(workDataOf(PROGRESS_STAGE_KEY to stage, PROGRESS_PERCENT_KEY to percent))
        }) {
            is BackupResult.Success -> Result.success(
                workDataOf(PROGRESS_STAGE_KEY to "Backup completed", PROGRESS_PERCENT_KEY to 100)
            )
            is BackupResult.Failure -> if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure(workDataOf(ERROR_MESSAGE_KEY to result.message))
            }
        }
    }

    companion object {
        const val TRIGGER_KEY = "backup_trigger"
        const val PROGRESS_STAGE_KEY = "backup_progress_stage"
        const val PROGRESS_PERCENT_KEY = "backup_progress_percent"
        const val ERROR_MESSAGE_KEY = "backup_error_message"
    }
}
