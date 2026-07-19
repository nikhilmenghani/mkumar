package com.mkumar.ui.components.containers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewColumn
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mkumar.App.Companion.globalClass
import com.mkumar.R
import com.mkumar.common.extension.DateFormat
import com.mkumar.data.DashboardAlignment
import com.mkumar.data.ThemePreference
import com.mkumar.data.emptyString
import com.mkumar.ui.components.items.PreferenceItem
import com.mkumar.ui.theme.LocalPreferencesManager
import com.mkumar.MainActivity
import com.mkumar.viewmodel.BackupUiState
import com.mkumar.viewmodel.BackupViewModel
import com.mkumar.ui.components.dialogs.ConfirmActionDialog
import com.mkumar.backup.RestoreOption
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayContainer(backupViewModel: BackupViewModel = hiltViewModel()) {
    val prefs = LocalPreferencesManager.current
    val context = LocalContext.current
    val backupState by backupViewModel.state.collectAsState()
    val backupQueue by backupViewModel.queue.collectAsState()
    var confirmRestore by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var selectedBackup by remember { mutableStateOf<RestoreOption?>(null) }
    var backupToDelete by remember { mutableStateOf<RestoreOption?>(null) }

    val dialog = globalClass.singleChoiceDialog
    val textDialog = globalClass.singleTextDialog
    val sliderDialog = globalClass.singleSliderDialog
    val displayPrefs = prefs.displayPrefs
    val githubPrefs = prefs.githubPrefs
    val invoicePrefs = prefs.invoicePrefs
    val dashboardPrefs = prefs.dashboardPrefs
    val backupPrefs = prefs.backupPrefs

    val foundBackups = (backupState as? BackupUiState.BackupsFound)?.backups.orEmpty()
    if (showQueue) {
        ModalBottomSheet(onDismissRequest = { showQueue = false }) {
            Column(Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Backup queue",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                if (backupQueue.isEmpty()) {
                    Text("No backup work is queued or running.", Modifier.padding(24.dp))
                } else {
                    backupQueue.forEach { item ->
                        val detail = buildString {
                            append(item.stage ?: item.state)
                            if (item.progress > 0) append(" • ${item.progress}%")
                            if (item.attempt > 0) append(" • retry ${item.attempt}")
                        }
                        PreferenceItem(
                            label = item.label,
                            supportingText = detail,
                            icon = Icons.Rounded.CloudSync,
                            trailingContent = if (item.state.equals("Running", true)) {
                                { CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp) }
                            } else null
                        )
                    }
                }
            }
        }
    } else if (foundBackups.isNotEmpty()) {
        ModalBottomSheet(onDismissRequest = backupViewModel::dismissBackups) {
            Column(Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Choose a backup",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                foundBackups.forEach { option ->
                    PreferenceItem(
                        label = formatUtcAsLocal(option.entry.createdAtUtc),
                        supportingText = "${option.entry.trigger.toDisplayTrigger()} • ${formatFileSize(option.entry.sizeBytes)}",
                        icon = Icons.Rounded.Restore,
                        trailingContent = {
                            IconButton(onClick = {
                                backupToDelete = option
                                backupViewModel.dismissBackups()
                                confirmDelete = true
                            }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete backup")
                            }
                        },
                        onClick = {
                            selectedBackup = option
                            backupViewModel.dismissBackups()
                            confirmRestore = true
                        }
                    )
                }
            }
        }
    }

    if (confirmDelete && backupToDelete != null) {
        ConfirmActionDialog(
            title = "Delete backup?",
            message = "Delete the backup from ${formatUtcAsLocal(backupToDelete!!.entry.createdAtUtc)}? The next older retained backup will become visible.",
            confirmLabel = "Delete",
            highlightConfirmAsDestructive = true,
            onDismiss = { confirmDelete = false },
            onConfirm = {
                confirmDelete = false
                backupToDelete?.let(backupViewModel::deleteBackup)
            }
        )
    }

    if (confirmRestore && selectedBackup != null) {
        ConfirmActionDialog(
            title = "Restore database?",
            message = "Restore ${formatUtcAsLocal(selectedBackup!!.entry.createdAtUtc)}? This replaces every database record on this device and restarts the app.",
            confirmLabel = "Restore",
            highlightConfirmAsDestructive = true,
            onDismiss = { confirmRestore = false },
            onConfirm = {
                confirmRestore = false
                selectedBackup?.let { option ->
                    backupViewModel.restore(option) {
                        (context as? MainActivity)?.restartApplicationAfterRestore()
                    }
                }
            }
        )
    }

    Container(title = stringResource(R.string.display)) {
        PreferenceItem(
            label = stringResource(R.string.use_dynamic_color),
            supportingText = emptyString,
            icon = Icons.Rounded.Palette,
            switchState = displayPrefs.useDynamicColor,
            onSwitchChange = { displayPrefs.useDynamicColor = it }
        )

        if (!displayPrefs.useDynamicColor) {
            PreferenceItem(
                label = stringResource(R.string.theme),
                supportingText = when (displayPrefs.theme) {
                    ThemePreference.LIGHT.ordinal -> stringResource(R.string.light)
                    ThemePreference.DARK.ordinal -> stringResource(R.string.dark)
                    else -> stringResource(R.string.follow_system)
                },
                icon = Icons.Rounded.Nightlight,
                onClick = {
                    dialog.show(
                        title = globalClass.getString(R.string.theme),
                        description = globalClass.getString(R.string.select_theme_preference),
                        choices = listOf(
                            globalClass.getString(R.string.light),
                            globalClass.getString(R.string.dark),
                            globalClass.getString(R.string.follow_system)
                        ),
                        selectedChoice = displayPrefs.theme,
                        onSelect = { displayPrefs.theme = it }
                    )
                }
            )
        }
    }

    Container(title = "Authentication") {
        PreferenceItem(
            label = "Github Token",
            supportingText = if (githubPrefs.token.isBlank()) "Not configured" else "Configured",
            icon = Icons.Rounded.VpnKey,
            onClick = {
                textDialog.show(
                    title = "Github Token",
                    description = "Enter your Github token",
                    text = githubPrefs.token,
                    onConfirm = {
                        githubPrefs.token = it.trim()
                        backupViewModel.reschedule()
                    }
                )
            }
        )

        PreferenceItem(
            label = "Github Owner",
            supportingText = githubPrefs.githubOwner,
            icon = Icons.Rounded.VpnKey,
            onClick = {
                textDialog.show(
                    title = "Github Owner",
                    description = "Enter your Github Owner",
                    text = githubPrefs.githubOwner,
                    onConfirm = { githubPrefs.githubOwner = it }
                )
            }
        )

        PreferenceItem(
            label = "Github Repository",
            supportingText = githubPrefs.githubRepo,
            icon = Icons.Rounded.VpnKey,
            onClick = {
                textDialog.show(
                    title = "Github Repository",
                    description = "Enter your Github Repository",
                    text = githubPrefs.githubRepo,
                    onConfirm = { githubPrefs.githubRepo = it }
                )
            }
        )
    }

    Container(title = "Database Backup") {
        val backupInProgress = (backupState as? BackupUiState.Working)?.isBackup == true
        PreferenceItem(
            label = "Backup interval",
            supportingText = backupIntervalLabel(backupPrefs.intervalHours),
            icon = Icons.Rounded.Backup,
            onClick = {
                val intervals = listOf(0, 6, 12, 24)
                dialog.show(
                    title = "Backup interval",
                    description = "Android runs scheduled backups approximately at this interval.",
                    choices = intervals.map(::backupIntervalLabel),
                    selectedChoice = intervals.indexOf(backupPrefs.intervalHours).coerceAtLeast(0),
                    onSelect = { index ->
                        backupPrefs.intervalHours = intervals[index]
                        backupViewModel.reschedule()
                    }
                )
            }
        )
        PreferenceItem(
            label = "Completed-order backups",
            supportingText = "Disabled for now",
            icon = Icons.Rounded.CloudSync
        )
        PreferenceItem(
            label = "Retained backups",
            supportingText = "Keep the latest ${backupPrefs.retentionCount}",
            icon = Icons.Rounded.Backup,
            onClick = {
                val counts = listOf(3, 6, 10, 20, 30)
                dialog.show(
                    title = "Retained backups",
                    description = "Older snapshots are pruned after the next successful backup.",
                    choices = counts.map { "Latest $it" },
                    selectedChoice = counts.indexOf(backupPrefs.retentionCount).coerceAtLeast(0),
                    onSelect = { backupPrefs.retentionCount = counts[it] }
                )
            }
        )
        PreferenceItem(
            label = "Backups shown",
            supportingText = displayCountLabel(backupPrefs.displayCount),
            icon = Icons.AutoMirrored.Rounded.ListAlt,
            onClick = {
                val counts = listOf(3, 6, 10, 20, 0)
                dialog.show(
                    title = "Backups shown",
                    description = "Choose how many retained restore points appear in the list.",
                    choices = counts.map(::displayCountLabel),
                    selectedChoice = counts.indexOf(backupPrefs.displayCount).coerceAtLeast(0),
                    onSelect = { backupPrefs.displayCount = counts[it] }
                )
            }
        )
        PreferenceItem(
            label = "Back up now",
            supportingText = backupPrefs.lastSuccessfulBackupAt
                .takeIf { it.isNotBlank() }
                ?.let(::formatUtcAsLocal)
                ?: "No successful backup yet",
            icon = Icons.Rounded.Backup,
            onClick = { if (!backupInProgress) backupViewModel.backupNow() },
            trailingContent = if (backupInProgress) {
                {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else null
        )
        PreferenceItem(
            label = "Find backup",
            supportingText = "Show ${displayCountLabel(backupPrefs.displayCount).lowercase()}",
            icon = Icons.Rounded.CloudSync,
            onClick = backupViewModel::findBackup
        )
        PreferenceItem(
            label = "Backup queue",
            supportingText = if (backupQueue.isEmpty()) "No queued or running work"
                else "${backupQueue.size} queued or running",
            icon = Icons.AutoMirrored.Rounded.ListAlt,
            onClick = { showQueue = true }
        )
        val status = when (val state = backupState) {
            BackupUiState.Idle -> backupPrefs.lastBackupError
            is BackupUiState.Working -> state.percent
                ?.takeIf { it > 0 }
                ?.let { "${state.message} ($it%)" }
                ?: state.message
            is BackupUiState.Message -> state.message
            is BackupUiState.Error -> state.message
            is BackupUiState.BackupsFound -> "Found ${state.backups.size} backup${if (state.backups.size == 1) "" else "s"}"
        }
        if (status.isNotBlank()) {
            PreferenceItem(
                label = "Backup status",
                supportingText = status,
                icon = Icons.Rounded.CloudSync,
                trailingContent = if (backupInProgress) {
                    {
                        val percent = (backupState as BackupUiState.Working).percent
                        if (percent != null && percent > 0) {
                            CircularProgressIndicator(
                                progress = { percent / 100f },
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                } else null
            )
        }
    }

    Container(title = "Invoice") {
        PreferenceItem(
            label = "Product Highlight Intensity",
            supportingText = invoicePrefs.productHighlightIntensity.toString(),
            icon = Icons.Rounded.Tune,
            onClick = {
                sliderDialog.show(
                    title = "Product Highlight Intensity",
                    sliderTitle = "Intensity",
                    description = "Controls the product highlights intensity in invoice",
                    value = invoicePrefs.productHighlightIntensity,
                    onConfirm = { invoicePrefs.productHighlightIntensity = it },
                    onDismiss = sliderDialog::dismiss
                )
            }
        )

        PreferenceItem(
            label = "Invoice Prefix",
            supportingText = invoicePrefs.invoicePrefix,
            icon = Icons.Rounded.LocalOffer,
            onClick = {
                textDialog.show(
                    title = "Invoice Prefix",
                    description = "Enter your invoice prefix",
                    text = invoicePrefs.invoicePrefix,
                    onConfirm = { invoicePrefs.invoicePrefix = it }
                )
            }
        )

        PreferenceItem(
            label = "Invoice Date Format",
            supportingText = DateFormat.entries[invoicePrefs.invoiceDateFormat].pattern,
            icon = Icons.Rounded.Event,
            onClick = {
                dialog.show(
                    title = "Invoice Date Format",
                    description = "Select your preferred date format for invoices",
                    choices = DateFormat.entries.map { it.pattern },
                    selectedChoice = invoicePrefs.invoiceDateFormat,
                    onSelect = { invoicePrefs.invoiceDateFormat = it }
                )
            }
        )
    }

    Container(title = "Dashboard") {
        PreferenceItem(
            label = "Dashboard Items Alignment",
            supportingText = when (dashboardPrefs.dashboardAlignment) {
                DashboardAlignment.VERTICAL.ordinal -> "Vertical"
                DashboardAlignment.HORIZONTAL.ordinal -> "Horizontal"
                else -> "Vertical"
            },
            icon = Icons.Rounded.ViewColumn,
            onClick = {
                dialog.show(
                    title = "Dashboard Items Alignment",
                    description = "Select dashboard items alignment",
                    choices = DashboardAlignment.entries.map { it.toString() },
                    selectedChoice = dashboardPrefs.dashboardAlignment,
                    onSelect = { dashboardPrefs.dashboardAlignment = it }
                )
            }
        )
    }
}

private fun backupIntervalLabel(hours: Int): String = when (hours) {
    0 -> "Off"
    6 -> "Every 6 hours"
    12 -> "Every 12 hours"
    24 -> "Every 24 hours"
    else -> "Every $hours hours"
}

private fun displayCountLabel(count: Int): String =
    if (count <= 0) "All retained backups" else "Latest $count backups"

private fun formatUtcAsLocal(timestamp: String): String = runCatching {
    LOCAL_BACKUP_TIME_FORMAT.format(Instant.parse(timestamp).atZone(ZoneId.systemDefault()))
}.getOrDefault(timestamp)

private fun String.toDisplayTrigger(): String = lowercase()
    .replace('_', ' ')
    .replaceFirstChar { it.uppercase() }

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}

private val LOCAL_BACKUP_TIME_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy • h:mm a z")
