package com.mkumar.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mkumar.common.manager.PackageManager.installApk

class UpdateActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_DOWNLOAD_UPDATE -> {
                val version = intent.getStringExtra(EXTRA_VERSION).orEmpty()
                val url = intent.getStringExtra(EXTRA_DOWNLOAD_URL).orEmpty()
                if (version.isNotBlank() && url.isNotBlank()) {
                    AppUpdateManager.enqueueDownload(context, version, url)
                }
            }
            ACTION_INSTALL_UPDATE -> {
                intent.getStringExtra(EXTRA_APK_PATH)
                    ?.takeIf(String::isNotBlank)
                    ?.let { installApk(context, it) }
            }
        }
    }

    companion object {
        const val ACTION_DOWNLOAD_UPDATE = "com.mkumar.action.DOWNLOAD_UPDATE"
        const val ACTION_INSTALL_UPDATE = "com.mkumar.action.INSTALL_UPDATE"
        const val EXTRA_VERSION = "update_version"
        const val EXTRA_DOWNLOAD_URL = "update_download_url"
        const val EXTRA_APK_PATH = "update_apk_path"
    }
}
