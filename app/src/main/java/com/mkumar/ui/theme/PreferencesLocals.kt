package com.mkumar.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.mkumar.data.PreferencesManager

val LocalPreferencesManager = staticCompositionLocalOf<PreferencesManager> {
    error("PreferencesManager not provided")
}
