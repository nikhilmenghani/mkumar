package com.mkumar.ui.previews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mkumar.ui.components.containers.BackupRestoreCard
import com.mkumar.ui.components.containers.Container
import com.mkumar.ui.components.items.PreferenceItem
import com.mkumar.ui.theme.NikThemePreview

@Composable
private fun PreferencesPreviewContent() {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Container(title = "Database Backup", initiallyExpanded = true) {
                PreferenceItem(
                    label = "Enable database backups",
                    supportingText = "Backup functionality is enabled",
                    icon = Icons.Rounded.Backup,
                    switchState = true,
                    onSwitchChange = {}
                )
                PreferenceItem(
                    label = "Find backups",
                    supportingText = "Browse latest 6 backups",
                    icon = Icons.Rounded.Restore
                )
                PreferenceItem(
                    label = "Backup queue",
                    supportingText = "1 running · Uploading to GitHub (55%)",
                    icon = Icons.AutoMirrored.Rounded.ListAlt
                )
            }
            Container(title = "Backup Preferences", initiallyExpanded = true) {
                PreferenceItem("Device name", "Galaxy S26 Ultra", Icons.Rounded.Devices)
                PreferenceItem("Scheduled backup interval", "Every 6 hours", Icons.Rounded.Schedule)
                PreferenceItem("GitHub token", "Configured", Icons.Rounded.VpnKey)
            }
            Container(title = "Restore cards", initiallyExpanded = true) {
                PreviewData.backups.forEach { option ->
                    BackupRestoreCard(option = option, onRestore = {}, onDelete = {})
                }
            }
        }
    }
}

@Preview(name = "Preferences · Light", showBackground = true, widthDp = 420, heightDp = 1100)
@Composable
private fun PreferencesLightPreview() = NikThemePreview { PreferencesPreviewContent() }

@Preview(name = "Preferences · Dark", showBackground = true, widthDp = 420, heightDp = 1100, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreferencesDarkPreview() = NikThemePreview { PreferencesPreviewContent() }
