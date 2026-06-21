package com.mkumar.common.constant

import com.mkumar.data.PreferencesManager

object PreferenceConstants {

    fun invoicePrefix(prefs: PreferencesManager): String {
        return prefs.invoicePrefs.invoicePrefix
    }
}
