package com.mkumar.common.constant

import android.Manifest
import android.provider.Settings
import com.mkumar.data.PermissionInfo

object PermissionConstants {
    const val INSTALL_APPS = "Install Unknown Apps"
    const val STORAGE = "Storage"
    const val NOTIFICATIONS = "Notifications"

    val permissionMap = mapOf(
        NOTIFICATIONS to PermissionInfo(
            permission = arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            rationale = "Notification permission is required to send you progress update notifications.",
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        ),
        STORAGE to PermissionInfo(
            permission = arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
            rationale = "Storage permission is required to download and operate on MKumar apk and zip files.",
            action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
        ),
        INSTALL_APPS to PermissionInfo(
            permission = arrayOf(Manifest.permission.REQUEST_INSTALL_PACKAGES),
            rationale = "Install unknown apps permission is for seamless MKumar app updates.",
            action = Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
        )
    )
}