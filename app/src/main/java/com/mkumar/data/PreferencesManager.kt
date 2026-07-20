package com.mkumar.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mkumar.common.extension.DateFormat
import com.mkumar.common.util.dataStoreMutableState
import javax.inject.Inject
import javax.inject.Singleton

const val emptyString = ""


@Singleton
class PreferencesManager @Inject constructor() {
    val displayPrefs = DisplayPrefs
    val githubPrefs = GithubPrefs
    val invoicePrefs = InvoicePrefs
    val dashboardPrefs = DashboardPrefs
    val backupPrefs = BackupPrefs
    val updatePrefs = UpdatePrefs
}

enum class ThemePreference {
    LIGHT,
    DARK,
    SYSTEM
}

enum class DashboardAlignment {
    HORIZONTAL,
    VERTICAL
}

object DisplayPrefs {
    var theme by dataStoreMutableState(
        keyName = "theme",
        defaultValue = ThemePreference.SYSTEM.ordinal,
        getPreferencesKey = { intPreferencesKey(it) }
    )

    var useDynamicColor by dataStoreMutableState(
        keyName = "useDynamicColor",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )
}

object GithubPrefs {
    var token by dataStoreMutableState(
        keyName = "token",
        defaultValue = emptyString,
        getPreferencesKey = { stringPreferencesKey(it) }
    )

    var githubOwner by dataStoreMutableState(
        keyName = "githubOwner",
        defaultValue = "nikhilmenghani",
        getPreferencesKey = { stringPreferencesKey(it) }
    )

    var githubRepo by dataStoreMutableState(
        keyName = "githubRepo",
        defaultValue = "tracker",
        getPreferencesKey = { stringPreferencesKey(it) }
    )
}

object InvoicePrefs {
    var productHighlightIntensity by dataStoreMutableState(
        keyName = "productHighlightIntensity",
        defaultValue = 65,
        getPreferencesKey = { intPreferencesKey(it) }
    )
    var invoicePrefix by dataStoreMutableState(
        keyName = "invoicePrefix",
        defaultValue = "MKumar-",
        getPreferencesKey = { stringPreferencesKey(it) }
    )
    var invoiceDateFormat by dataStoreMutableState(
        keyName = "invoiceDateFormat",
        defaultValue = DateFormat.DEFAULT_DATE_ONLY.ordinal,
        getPreferencesKey = { intPreferencesKey(it) }
    )
}

object DashboardPrefs {
    var showCustomerCount by dataStoreMutableState(
        keyName = "showCustomerCount",
        defaultValue = true,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )
    var showTotalSales by dataStoreMutableState(
        keyName = "showTotalSales",
        defaultValue = true,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )
    var showTotalPayments by dataStoreMutableState(
        keyName = "showTotalPayments",
        defaultValue = true,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )
    var showTotalOutstanding by dataStoreMutableState(
        keyName = "showTotalOutstanding",
        defaultValue = true,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )
    var dashboardAlignment by dataStoreMutableState(
        keyName = "dashboardAlignment",
        defaultValue = DashboardAlignment.VERTICAL.ordinal,
        getPreferencesKey = { intPreferencesKey(it) }
    )
}

object BackupPrefs {
    var enabled by dataStoreMutableState(
        keyName = "backupFeatureEnabledV2",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )
    var backupOnOrderCompleted by dataStoreMutableState(
        keyName = "backupOnOrderCompletedV2",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )
    var intervalHours by dataStoreMutableState(
        keyName = "backupIntervalHoursV2",
        defaultValue = 0,
        getPreferencesKey = { intPreferencesKey(it) }
    )
    var retentionCount by dataStoreMutableState(
        keyName = "backupRetentionCountV2",
        defaultValue = 10,
        getPreferencesKey = { intPreferencesKey(it) }
    )
    var displayCount by dataStoreMutableState(
        keyName = "backupDisplayCount",
        defaultValue = 6,
        getPreferencesKey = { intPreferencesKey(it) }
    )
    var deviceId by dataStoreMutableState(
        keyName = "backupDeviceId",
        defaultValue = "",
        getPreferencesKey = { stringPreferencesKey(it) }
    )
    var deviceName by dataStoreMutableState(
        keyName = "backupDeviceName",
        defaultValue = "",
        getPreferencesKey = { stringPreferencesKey(it) }
    )
    var lastSuccessfulBackupAt by dataStoreMutableState(
        keyName = "lastSuccessfulBackupAt",
        defaultValue = "",
        getPreferencesKey = { stringPreferencesKey(it) }
    )
    var lastBackupError by dataStoreMutableState(
        keyName = "lastBackupError",
        defaultValue = "",
        getPreferencesKey = { stringPreferencesKey(it) }
    )
}

object UpdatePrefs {
    var intervalHours by dataStoreMutableState(
        keyName = "updateCheckIntervalHours",
        defaultValue = 0,
        getPreferencesKey = { intPreferencesKey(it) }
    )
}
