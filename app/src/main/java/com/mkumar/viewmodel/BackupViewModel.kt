package com.mkumar.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mkumar.backup.BackupRestoreManager
import com.mkumar.backup.BackupScheduler
import com.mkumar.backup.RestoreResult
import com.mkumar.backup.RestoreOption
import com.mkumar.backup.worker.DatabaseBackupWorker
import com.mkumar.data.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val scheduler: BackupScheduler,
    private val restoreManager: BackupRestoreManager,
    private val preferences: PreferencesManager
) : ViewModel() {
    private val workManager = WorkManager.getInstance(context)
    private var manualBackupLiveData: LiveData<WorkInfo?>? = null
    private var manualBackupObserver: Observer<WorkInfo?>? = null
    private val queueLiveData = workManager.getWorkInfosByTagLiveData(BackupScheduler.BACKUP_WORK_TAG)
    private var latestQueueWork: List<WorkInfo> = emptyList()
    private val queueObserver = Observer<List<WorkInfo>> { work ->
        latestQueueWork = work.orEmpty()
        updateQueue(latestQueueWork)
    }
    private val _state = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val state: StateFlow<BackupUiState> = _state.asStateFlow()
    private val _queue = MutableStateFlow<List<BackupQueueItem>>(emptyList())
    val queue: StateFlow<List<BackupQueueItem>> = _queue.asStateFlow()

    init {
        queueLiveData.observeForever(queueObserver)
        viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                updateQueue(latestQueueWork)
            }
        }
    }

    fun backupNow() {
        if (!preferences.backupPrefs.enabled) {
            _state.value = BackupUiState.Error("Enable database backups before starting a manual backup.")
            return
        }
        val workId = scheduler.enqueueManual()
        _state.value = BackupUiState.Message("Backup queued. Waiting for network and battery constraints.")
        stopObservingManualBackup()
        val liveData = workManager.getWorkInfoByIdLiveData(workId)
        val observer = Observer<WorkInfo?> { info ->
            if (info != null) {
                _state.value = when (info.state) {
                    WorkInfo.State.BLOCKED -> BackupUiState.Message("Backup queued behind another task.")
                    WorkInfo.State.ENQUEUED -> {
                        if (info.runAttemptCount > 0) BackupUiState.Message("Backup retry scheduled.")
                        else BackupUiState.Message("Backup queued. Waiting for network and battery constraints.")
                    }
                    WorkInfo.State.RUNNING -> BackupUiState.Working(
                        message = info.progress.getString(DatabaseBackupWorker.PROGRESS_STAGE_KEY)
                            ?: "Backing up database…",
                        percent = info.progress.getInt(DatabaseBackupWorker.PROGRESS_PERCENT_KEY, 0),
                        isBackup = true
                    )
                    WorkInfo.State.SUCCEEDED -> BackupUiState.Message("Backup completed successfully.")
                    WorkInfo.State.FAILED -> BackupUiState.Error(
                        info.outputData.getString(DatabaseBackupWorker.ERROR_MESSAGE_KEY)
                            ?: "Backup failed. Please retry."
                    )
                    WorkInfo.State.CANCELLED -> BackupUiState.Error("Backup was cancelled.")
                }
                if (info.state.isFinished) stopObservingManualBackup()
            }
        }
        manualBackupLiveData = liveData
        manualBackupObserver = observer
        liveData.observeForever(observer)
    }

    fun reschedule() = scheduler.schedulePeriodic()

    fun setBackupEnabled(enabled: Boolean) {
        preferences.backupPrefs.enabled = enabled
        if (enabled) {
            scheduler.schedulePeriodic()
        } else {
            scheduler.cancelAllBackupWork()
            _state.value = BackupUiState.Idle
        }
    }

    private fun stopObservingManualBackup() {
        val liveData = manualBackupLiveData
        val observer = manualBackupObserver
        if (liveData != null && observer != null) liveData.removeObserver(observer)
        manualBackupLiveData = null
        manualBackupObserver = null
    }

    override fun onCleared() {
        stopObservingManualBackup()
        queueLiveData.removeObserver(queueObserver)
        super.onCleared()
    }

    fun findBackup() {
        if (!requireBackupsEnabled()) return
        viewModelScope.launch {
            _state.value = BackupUiState.Working("Looking for a backup…")
            _state.value = runCatching { restoreManager.findBackups() }
                .fold(
                    onSuccess = { backups ->
                        val visible = backups.visibleBackups()
                        if (visible.isEmpty()) BackupUiState.Error("No M Kumar backup was found")
                        else BackupUiState.BackupsFound(visible)
                    },
                    onFailure = { BackupUiState.Error(it.message ?: "Backup discovery failed") }
                )
        }
    }

    fun deleteBackup(option: RestoreOption) {
        if (!requireBackupsEnabled()) return
        viewModelScope.launch {
            _state.value = BackupUiState.Working("Deleting backup…")
            _state.value = runCatching { restoreManager.delete(option).visibleBackups() }
                .fold(
                    onSuccess = { remaining ->
                        if (remaining.isEmpty()) BackupUiState.Error("No backups remain")
                        else BackupUiState.BackupsFound(remaining)
                    },
                    onFailure = { BackupUiState.Error(it.message ?: "Could not delete backup") }
                )
        }
    }

    private fun List<RestoreOption>.visibleBackups(): List<RestoreOption> {
        val count = preferences.backupPrefs.displayCount
        return if (count <= 0) this else take(count)
    }

    private fun updateQueue(work: List<WorkInfo>) {
        val now = System.currentTimeMillis()
        _queue.value = work
            .filterNot { it.state.isFinished }
            .filter { info ->
                val isScheduled = BackupScheduler.SCHEDULED_WORK_TAG in info.tags
                !isScheduled || info.state == WorkInfo.State.RUNNING ||
                    info.state == WorkInfo.State.BLOCKED ||
                    (info.nextScheduleTimeMillis > 0 && info.nextScheduleTimeMillis <= now)
            }
            .sortedBy { if (it.state == WorkInfo.State.RUNNING) 0 else 1 }
            .map { info ->
                BackupQueueItem(
                    id = info.id.toString(),
                    label = when {
                        BackupScheduler.MANUAL_WORK_TAG in info.tags -> "Manual backup"
                        BackupScheduler.EVENT_WORK_TAG in info.tags -> "Event backup"
                        else -> "Scheduled backup"
                    },
                    state = info.state.name.lowercase().replaceFirstChar { it.uppercase() },
                    attempt = info.runAttemptCount,
                    progress = info.progress.getInt(DatabaseBackupWorker.PROGRESS_PERCENT_KEY, 0),
                    stage = info.progress.getString(DatabaseBackupWorker.PROGRESS_STAGE_KEY)
                )
            }
    }

    fun dismissBackups() {
        if (_state.value is BackupUiState.BackupsFound) _state.value = BackupUiState.Idle
    }

    fun restore(option: RestoreOption, onDatabaseReplaced: () -> Unit) {
        if (!requireBackupsEnabled()) return
        viewModelScope.launch {
            _state.value = BackupUiState.Working("Downloading and validating backup…")
            when (val result = restoreManager.restore(option)) {
                is RestoreResult.Success -> {
                    _state.value = BackupUiState.Message("Restore completed. Restarting…")
                    onDatabaseReplaced()
                }
                is RestoreResult.Failure -> {
                    _state.value = BackupUiState.Error(result.message)
                    if (result.restartRequired) onDatabaseReplaced()
                }
            }
        }
    }

    private fun requireBackupsEnabled(): Boolean {
        if (preferences.backupPrefs.enabled) return true
        _state.value = BackupUiState.Error("Enable database backups to use this feature.")
        return false
    }
}

data class BackupQueueItem(
    val id: String,
    val label: String,
    val state: String,
    val attempt: Int,
    val progress: Int,
    val stage: String?
)

sealed interface BackupUiState {
    data object Idle : BackupUiState
    data class Working(
        val message: String,
        val percent: Int? = null,
        val isBackup: Boolean = false
    ) : BackupUiState
    data class Message(val message: String) : BackupUiState
    data class Error(val message: String) : BackupUiState
    data class BackupsFound(val backups: List<RestoreOption>) : BackupUiState
}
