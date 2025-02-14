package com.mkumar.common

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object PackageManager {
    fun getCurrentVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
            "Unknown"
        }.toString()
    }

    fun installApk(context: Context, apkPath: String) {
        try {
            val apkFile = File(apkPath)
            if (apkFile.exists()) {
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    apkFile
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
            } else {
                Log.e("MKumar-InstallAPK", "APK file does not exist: $apkPath")
                Toast.makeText(context, "APK file does not exist: $apkPath", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MKumar-InstallAPK", "Error installing APK: ${e.message}")
            Toast.makeText(context, "Error installing APK: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun openSettings(context: Context, action: String) {
        val intent = Intent(action).apply {
            when (action) {
                Settings.ACTION_APP_NOTIFICATION_SETTINGS -> {
                    putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
                }
                else -> {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            }
        }
        context.startActivity(intent)
    }
}