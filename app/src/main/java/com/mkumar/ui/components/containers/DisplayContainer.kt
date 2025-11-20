package com.mkumar.ui.components.containers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewColumn
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mkumar.App.Companion.globalClass
import com.mkumar.R
import com.mkumar.common.extension.DateFormat
import com.mkumar.data.DashboardAlignment
import com.mkumar.data.ThemePreference
import com.mkumar.data.emptyString
import com.mkumar.ui.components.items.PreferenceItem

@Composable
fun DisplayContainer() {
    val dialog = globalClass.singleChoiceDialog
    val textDialog = globalClass.singleTextDialog
    val sliderDialog = globalClass.singleSliderDialog
    val preferences = globalClass.preferencesManager.displayPrefs
    val githubPreference = globalClass.preferencesManager.githubPrefs
    val invoicePreference = globalClass.preferencesManager.invoicePrefs
    val dashboardPreference = globalClass.preferencesManager.dashboardPrefs

    Container(title = stringResource(R.string.display)) {
        PreferenceItem(
            label = stringResource(R.string.use_dynamic_color),
            supportingText = emptyString,
            icon = Icons.Rounded.Palette,
            switchState = preferences.useDynamicColor,
            onSwitchChange = { preferences.useDynamicColor = it }
        )

        if (!preferences.useDynamicColor) {
            PreferenceItem(
                label = stringResource(R.string.theme),
                supportingText = when (preferences.theme) {
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
                        selectedChoice = preferences.theme,
                        onSelect = { preferences.theme = it }
                    )
                }
            )
        }
    }

    Container(title = "Authentication") {
        PreferenceItem(
            label = "Github Token",
            supportingText = githubPreference.token,
            icon = Icons.Rounded.VpnKey,
            onClick = {
                textDialog.show(
                    title = "Github Token",
                    description = "Enter your Github token",
                    text = githubPreference.token,
                    onConfirm = { githubPreference.token = it }
                )
            }
        )
    }

    Container(title = "Invoice") {
        PreferenceItem(
            label = "Product Highlight Intensity",
            supportingText = invoicePreference.productHighlightIntensity.toString(),
            icon = Icons.Rounded.Tune,
            onClick = {
                sliderDialog.show(
                    title = "Product Highlight Intensity",
                    sliderTitle = "Intensity",
                    description = "Controls the product highlights intensity in invoice",
                    value = invoicePreference.productHighlightIntensity,
                    onConfirm = { invoicePreference.productHighlightIntensity = it },
                    onDismiss = sliderDialog::dismiss
                )
            }
        )

        PreferenceItem(
            label = "Invoice Prefix",
            supportingText = invoicePreference.invoicePrefix,
            icon = Icons.Rounded.LocalOffer,
            onClick = {
                textDialog.show(
                    title = "Invoice Prefix",
                    description = "Enter your invoice prefix",
                    text = invoicePreference.invoicePrefix,
                    onConfirm = { invoicePreference.invoicePrefix = it }
                )
            }
        )

        PreferenceItem(
            label = "Invoice Date Format",
            supportingText = DateFormat.entries[invoicePreference.invoiceDateFormat].pattern,
            icon = Icons.Rounded.Event,
            onClick = {
                dialog.show(
                    title = "Invoice Date Format",
                    description = "Select your preferred date format for invoices",
                    choices = DateFormat.entries.map { it.pattern },
                    selectedChoice = invoicePreference.invoiceDateFormat,
                    onSelect = { invoicePreference.invoiceDateFormat = it }
                )
            }
        )
    }

    Container(title = "Dashboard") {
        PreferenceItem(
            label = "Dashboard Items Alignment",
            supportingText = when (dashboardPreference.dashboardAlignment) {
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
                    selectedChoice = dashboardPreference.dashboardAlignment,
                    onSelect = { dashboardPreference.dashboardAlignment = it }
                )
            }
        )
    }
}