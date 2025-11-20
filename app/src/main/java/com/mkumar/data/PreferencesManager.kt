package com.mkumar.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mkumar.common.extension.DateFormat
import com.mkumar.common.util.dataStoreMutableState

const val emptyString = ""

class PreferencesManager {
    val displayPrefs = DisplayPrefs
    val githubPrefs = GithubPrefs
    val invoicePrefs = InvoicePrefs
    val dashboardPrefs = DashboardPrefs
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