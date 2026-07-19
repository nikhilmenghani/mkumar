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
import androidx.compose.material.icons.rounded.Wifi
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

@Composable
fun DisplayContainer(backupViewModel: BackupViewModel = hiltViewModel()) {
    val prefs = LocalPreferencesManager.current
    val context = LocalContext.current
    val backupState by backupViewModel.state.collectAsState()
    var confirmRestore by remember { mutableStateOf(false) }

    val dialog = globalClass.singleChoiceDialog
    val textDialog = globalClass.singleTextDialog
    val sliderDialog = globalClass.singleSliderDialog
    val displayPrefs = prefs.displayPrefs
    val githubPrefs = prefs.githubPrefs
    val invoicePrefs = prefs.invoicePrefs
    val dashboardPrefs = prefs.dashboardPrefs
    val backupPrefs = prefs.backupPrefs

    if (confirmRestore) {
        ConfirmActionDialog(
            title = "Restore database?",
            message = "This replaces every customer, order, payment, and other database record on this device. The app will restart afterward.",
            confirmLabel = "Restore",
            highlightConfirmAsDestructive = true,
            onDismiss = { confirmRestore = false },
            onConfirm = {
                confirmRestore = false
                backupViewModel.restoreLatest {
                    (context as? MainActivity)?.restartApplicationAfterRestore()
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
        PreferenceItem(
            label = "Twice-daily backup",
            supportingText = "Approximately every 12 hours",
            icon = Icons.Rounded.Backup,
            switchState = backupPrefs.enabled,
            onSwitchChange = {
                backupPrefs.enabled = it
                backupViewModel.reschedule()
            }
        )
        PreferenceItem(
            label = "Back up completed orders",
            supportingText = "Debounced after an order first becomes complete",
            icon = Icons.Rounded.CloudSync,
            switchState = backupPrefs.backupOnOrderCompleted,
            onSwitchChange = { backupPrefs.backupOnOrderCompleted = it }
        )
        PreferenceItem(
            label = "Wi-Fi only",
            supportingText = "Use an unmetered connection for automatic backups",
            icon = Icons.Rounded.Wifi,
            switchState = backupPrefs.wifiOnly,
            onSwitchChange = {
                backupPrefs.wifiOnly = it
                backupViewModel.reschedule()
            }
        )
        PreferenceItem(
            label = "Back up now",
            supportingText = backupPrefs.lastSuccessfulBackupAt.ifBlank { "No successful backup yet" },
            icon = Icons.Rounded.Backup,
            onClick = backupViewModel::backupNow
        )
        PreferenceItem(
            label = "Find backup",
            supportingText = "Discover the repository using its backup manifest",
            icon = Icons.Rounded.CloudSync,
            onClick = backupViewModel::findBackup
        )
        PreferenceItem(
            label = "Restore latest backup",
            supportingText = "Replaces all data on this device",
            icon = Icons.Rounded.Restore,
            onClick = { confirmRestore = true }
        )
        val status = when (val state = backupState) {
            BackupUiState.Idle -> backupPrefs.lastBackupError
            is BackupUiState.Working -> state.message
            is BackupUiState.Message -> state.message
            is BackupUiState.Error -> state.message
        }
        if (status.isNotBlank()) {
            PreferenceItem(
                label = "Backup status",
                supportingText = status,
                icon = Icons.Rounded.CloudSync
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
