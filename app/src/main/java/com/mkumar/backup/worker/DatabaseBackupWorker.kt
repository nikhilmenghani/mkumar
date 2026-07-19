package com.mkumar.backup.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
        return when (coordinator.backup(trigger)) {
            is BackupResult.Success -> Result.success()
            is BackupResult.Failure -> if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val TRIGGER_KEY = "backup_trigger"
    }
}
