package com.mkumar.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.mkumar.common.util.dataStoreMutableState

const val emptyString = ""

class PreferencesManager {
    val displayPrefs = DisplayPrefs
//    val githubPrefs = GithubPrefs
}

enum class ThemePreference {
    LIGHT,
    DARK,
    SYSTEM
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