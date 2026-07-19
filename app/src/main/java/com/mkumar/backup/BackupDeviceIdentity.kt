package com.mkumar.backup

import android.os.Build

fun defaultBackupDeviceName(): String {
    val manufacturer = Build.MANUFACTURER.trim()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    val model = Build.MODEL.trim()

    return when {
        model.isBlank() -> manufacturer.ifBlank { "Android device" }
        manufacturer.isBlank() || model.startsWith(manufacturer, ignoreCase = true) -> model
        else -> "$manufacturer $model"
    }
}
