package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.backup.BackupRestoreManager
import com.mkumar.backup.BackupScheduler
import com.mkumar.backup.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val scheduler: BackupScheduler,
    private val restoreManager: BackupRestoreManager
) : ViewModel() {
    private val _state = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    fun backupNow() {
        scheduler.enqueueManual()
        _state.value = BackupUiState.Message("Backup queued. It will run when network and battery constraints allow.")
    }

    fun reschedule() = scheduler.schedulePeriodic()

    fun findBackup() {
        viewModelScope.launch {
            _state.value = BackupUiState.Working("Looking for a backup…")
            _state.value = runCatching { restoreManager.findBackup() }
                .fold(
                    onSuccess = { backup ->
                        if (backup == null) BackupUiState.Error("No M Kumar backup was found")
                        else BackupUiState.Message(
                            "Found ${backup.manifest.createdAtUtc} in ${backup.owner}/${backup.repository}"
                        )
                    },
                    onFailure = { BackupUiState.Error(it.message ?: "Backup discovery failed") }
                )
        }
    }

    fun restoreLatest(onDatabaseReplaced: () -> Unit) {
        viewModelScope.launch {
            _state.value = BackupUiState.Working("Downloading and validating backup…")
            when (val result = restoreManager.restoreLatest()) {
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
}

sealed interface BackupUiState {
    data object Idle : BackupUiState
    data class Working(val message: String) : BackupUiState
    data class Message(val message: String) : BackupUiState
    data class Error(val message: String) : BackupUiState
}
