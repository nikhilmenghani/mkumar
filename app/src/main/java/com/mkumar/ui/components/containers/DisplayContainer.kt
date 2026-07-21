package com.mkumar.ui.components.containers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.mkumar.App.Companion.globalClass
import com.mkumar.R
import com.mkumar.common.extension.DateFormat
import com.mkumar.data.ThemePreference
import com.mkumar.data.emptyString
import com.mkumar.ui.components.items.PreferenceItem
import com.mkumar.ui.components.items.PreferenceSubtitle
import com.mkumar.ui.theme.LocalPreferencesManager
import com.mkumar.MainActivity
import com.mkumar.viewmodel.BackupUiState
import com.mkumar.viewmodel.BackupViewModel
import com.mkumar.ui.components.dialogs.ConfirmActionDialog
import com.mkumar.backup.RestoreOption
import com.mkumar.backup.defaultBackupDeviceName
import com.mkumar.update.AppUpdateManager
import com.mkumar.common.manager.PackageManager.getCurrentVersion
import java.time.Instant
import java.time.Duration
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
    var confirmBackup by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var selectedBackup by remember { mutableStateOf<RestoreOption?>(null) }
    var backupToDelete by remember { mutableStateOf<RestoreOption?>(null) }
    var developerTapCount by remember { mutableStateOf(0) }

    val dialog = globalClass.singleChoiceDialog
    val textDialog = globalClass.singleTextDialog
    val sliderDialog = globalClass.singleSliderDialog
    val displayPrefs = prefs.displayPrefs
    val githubPrefs = prefs.githubPrefs
    val invoicePrefs = prefs.invoicePrefs
    val backupPrefs = prefs.backupPrefs
    val updatePrefs = prefs.updatePrefs
    val developerPrefs = prefs.developerPrefs

    LaunchedEffect(backupPrefs.deviceName) {
        if (backupPrefs.deviceName.isBlank()) {
            backupPrefs.deviceName = defaultBackupDeviceName()
        }
    }

    LaunchedEffect(backupPrefs.enabled) {
        if (!backupPrefs.enabled) {
            confirmRestore = false
            confirmDelete = false
            confirmBackup = false
            showQueue = false
            backupViewModel.dismissBackups()
        }
    }

    LaunchedEffect(backupQueue.isEmpty()) {
        if (backupQueue.isEmpty()) showQueue = false
    }

    val foundBackups = (backupState as? BackupUiState.BackupsFound)?.backups.orEmpty()
    val backupInProgress = (backupState as? BackupUiState.Working)?.isBackup == true
    if (showQueue && backupQueue.isNotEmpty()) {
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
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Text(
                        text = "Choose a backup",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
                items(
                    items = foundBackups,
                    key = { it.entry.backupPath }
                ) { option ->
                    BackupRestoreCard(
                        option = option,
                        onRestore = {
                            selectedBackup = option
                            backupViewModel.dismissBackups()
                            confirmRestore = true
                        },
                        onDelete = {
                            backupToDelete = option
                            backupViewModel.dismissBackups()
                            confirmDelete = true
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

    if (confirmBackup) {
        ConfirmActionDialog(
            title = "Back up database now?",
            message = "Create a new database backup in the configured GitHub repository?",
            confirmLabel = "Back up",
            onDismiss = { confirmBackup = false },
            onConfirm = {
                confirmBackup = false
                backupViewModel.backupNow()
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

    val developerOptionsContent: @Composable () -> Unit = {
        Container(title = "Developer Options") {
            PreferenceSubtitle(text = "Experimental features")
            PreferenceItem(
                label = "Experimental features",
                supportingText = if (developerPrefs.experimentalFeaturesEnabled) "Experimental features are available" else "Experimental features are hidden",
                icon = Icons.Rounded.Science,
                switchState = developerPrefs.experimentalFeaturesEnabled,
                onSwitchChange = { developerPrefs.experimentalFeaturesEnabled = it }
            )
            if (developerPrefs.experimentalFeaturesEnabled) {
                PreferenceItem(
                    label = "Invoice sharing",
                    supportingText = if (developerPrefs.whatsappSharingEnabled) "Direct invoice sharing is enabled" else "Direct invoice sharing is disabled",
                    icon = Icons.AutoMirrored.Rounded.Message,
                    switchState = developerPrefs.whatsappSharingEnabled,
                    onSwitchChange = { developerPrefs.whatsappSharingEnabled = it }
                )
            }
            PreferenceSubtitle(text = "Backup preferences")
            PreferenceItem(
                label = "Device name",
                supportingText = backupPrefs.deviceName,
                icon = Icons.Rounded.Devices,
                enabled = backupPrefs.enabled,
                onClick = {
                    textDialog.show(
                        title = "Backup device name",
                        description = "Device name",
                        text = backupPrefs.deviceName,
                        onConfirm = { backupPrefs.deviceName = it.trim() }
                    )
                }
            )
            PreferenceItem(
                label = "Scheduled backup interval",
                supportingText = backupIntervalLabel(backupPrefs.intervalHours),
                icon = Icons.Rounded.Schedule,
                enabled = backupPrefs.enabled,
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
                label = "Retained backups",
                supportingText = "Keep the latest ${backupPrefs.retentionCount}",
                icon = Icons.Rounded.History,
                enabled = backupPrefs.enabled,
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
                icon = Icons.Rounded.Visibility,
                enabled = backupPrefs.enabled,
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
            PreferenceSubtitle(text = "Invoice")
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
            PreferenceSubtitle(text = "Developer access")
            PreferenceItem(
                label = "Hide developer options",
                supportingText = "Tap the developer name seven times to show them again",
                icon = Icons.Rounded.VisibilityOff,
                onClick = { developerPrefs.developerOptionsEnabled = false }
            )
        }
    }

    Container(title = "Database Backup") {
        PreferenceSubtitle(text = "Backup controls")
        PreferenceItem(
            label = "Enable database backups",
            supportingText = if (backupPrefs.enabled) "Backup functionality is enabled" else "Backups are off",
            icon = Icons.Rounded.Backup,
            switchState = backupPrefs.enabled,
            onSwitchChange = backupViewModel::setBackupEnabled
        )
        PreferenceItem(
            label = "Find backups",
            supportingText = "Browse ${displayCountLabel(backupPrefs.displayCount).lowercase()}",
            icon = Icons.Rounded.Restore,
            enabled = backupPrefs.enabled,
            onClick = backupViewModel::findBackup
        )
        if (backupQueue.isNotEmpty()) {
            PreferenceItem(
                label = "Backup queue",
                supportingText = "${backupQueue.size} queued or running",
                icon = Icons.AutoMirrored.Rounded.ListAlt,
                enabled = backupPrefs.enabled,
                onClick = { showQueue = true }
            )
        }
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
        PreferenceSubtitle(text = "Manual backup")
        PreferenceItem(
            label = "Back up now",
            supportingText = if (!backupPrefs.enabled) {
                "Enable database backups first"
            } else {
                backupPrefs.lastSuccessfulBackupAt
                    .takeIf { it.isNotBlank() }
                    ?.let {
                        "Last successful: ${formatUtcAsLocal(it)} • ${formatElapsedSince(it)}"
                    }
                    ?: "No successful backup yet"
            },
            icon = Icons.Rounded.CloudUpload,
            enabled = backupPrefs.enabled && !backupInProgress,
            onClick = { confirmBackup = true },
            trailingContent = if (backupInProgress) {
                {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else null
        )
        PreferenceSubtitle(text = "GitHub storage")
        PreferenceItem(
            label = "GitHub token",
            supportingText = if (githubPrefs.token.isBlank()) "Not configured" else "Configured",
            icon = Icons.Rounded.VpnKey,
            enabled = backupPrefs.enabled,
            onClick = {
                textDialog.show(
                    title = "GitHub token",
                    description = "Enter your GitHub token",
                    text = githubPrefs.token,
                    onConfirm = {
                        githubPrefs.token = it.trim()
                        backupViewModel.reschedule()
                    }
                )
            }
        )
        PreferenceItem(
            label = "Repository owner",
            supportingText = githubPrefs.githubOwner,
            icon = Icons.Rounded.AccountCircle,
            enabled = backupPrefs.enabled,
            onClick = {
                textDialog.show(
                    title = "GitHub owner",
                    description = "Enter the GitHub repository owner",
                    text = githubPrefs.githubOwner,
                    onConfirm = { githubPrefs.githubOwner = it.trim() }
                )
            }
        )
        PreferenceItem(
            label = "Repository name",
            supportingText = githubPrefs.githubRepo,
            icon = Icons.Rounded.Folder,
            enabled = backupPrefs.enabled,
            onClick = {
                textDialog.show(
                    title = "GitHub repository",
                    description = "Enter the GitHub repository name",
                    text = githubPrefs.githubRepo,
                    onConfirm = { githubPrefs.githubRepo = it.trim() }
                )
            }
        )
    }

    Container(title = "About App") {
        PreferenceSubtitle(text = "App updates")
        PreferenceItem(
            label = "Background update checks",
            supportingText = updateCheckIntervalLabel(updatePrefs.intervalHours),
            icon = Icons.Rounded.Schedule,
            onClick = {
                val intervals = listOf(0, 1, 3, 6, 12, 24)
                dialog.show(
                    title = "Update check interval",
                    description = "The app always checks once when opened. Choose an additional background interval.",
                    choices = intervals.map(::updateCheckIntervalLabel),
                    selectedChoice = intervals.indexOf(updatePrefs.intervalHours).coerceAtLeast(0),
                    onSelect = { index ->
                        updatePrefs.intervalHours = intervals[index]
                        AppUpdateManager.scheduleChecks(context, intervals[index])
                    }
                )
            }
        )
        PreferenceSubtitle(text = "Application information")
        PreferenceItem(
            label = "MKumar",
            supportingText = "Version ${getCurrentVersion(context)}",
            icon = Icons.Rounded.Info
        )
        PreferenceItem(
            label = "Nikhil Menghani",
            supportingText = "Developer",
            icon = Icons.Rounded.AccountCircle,
            onClick = {
                if (developerPrefs.developerOptionsEnabled) {
                    Toast.makeText(
                        context,
                        "Developer options are already enabled",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    developerTapCount++
                    if (developerTapCount >= 7) {
                        developerPrefs.developerOptionsEnabled = true
                        developerTapCount = 0
                        Toast.makeText(
                            context,
                            "Developer options enabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    if (developerPrefs.developerOptionsEnabled) {
        developerOptionsContent()
    }

}

@Composable
internal fun BackupRestoreCard(
    option: RestoreOption,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val entry = option.entry
    Card(
        onClick = onRestore,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Restore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = formatUtcAsLocal(entry.createdAtUtc),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = formatElapsedSince(entry.createdAtUtc),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Delete backup",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Text(
                text = entry.deviceName.ifBlank { "Unknown device" },
                style = MaterialTheme.typography.bodyLarge
            )
            val shortDeviceId = entry.deviceId.take(8)
            if (shortDeviceId.isNotBlank()) {
                Text(
                    text = "Device ID: $shortDeviceId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${entry.trigger.toDisplayTrigger()} • ${formatFileSize(entry.sizeBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap to restore",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

private fun backupIntervalLabel(hours: Int): String = when (hours) {
    0 -> "Off"
    6 -> "Every 6 hours"
    12 -> "Every 12 hours"
    24 -> "Every 24 hours"
    else -> "Every $hours hours"
}

private fun updateCheckIntervalLabel(hours: Int): String = when (hours) {
    0 -> "Off (check when app opens only)"
    1 -> "Every hour"
    else -> "Every $hours hours"
}

private fun displayCountLabel(count: Int): String =
    if (count <= 0) "All retained backups" else "Latest $count backups"

private fun formatUtcAsLocal(timestamp: String): String = runCatching {
    LOCAL_BACKUP_TIME_FORMAT.format(Instant.parse(timestamp).atZone(ZoneId.systemDefault()))
}.getOrDefault(timestamp)

private fun formatElapsedSince(timestamp: String, now: Instant = Instant.now()): String = runCatching {
    val elapsedMinutes = Duration.between(Instant.parse(timestamp), now)
        .toMinutes()
        .coerceAtLeast(0)
    val days = elapsedMinutes / (24 * 60)
    val hours = (elapsedMinutes % (24 * 60)) / 60
    val minutes = elapsedMinutes % 60
    when {
        days > 0 -> buildString {
            append(days).append(if (days == 1L) " day" else " days")
            if (hours > 0) append(" ").append(hours)
                .append(if (hours == 1L) " hour" else " hours")
            append(" ago")
        }
        hours > 0 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
        minutes > 0 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
        else -> "Just now"
    }
}.getOrDefault("Time unavailable")

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
