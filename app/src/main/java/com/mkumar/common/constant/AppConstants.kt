package com.mkumar.common.constant

import android.os.Environment

object AppConstants {

    const val latestVersionUrl = "https://api.github.com/repos/nikhilmenghani/mkumar/releases/latest"

    fun getAppDownloadUrl(latestVersion: String): String {
        return "https://github.com/nikhilmenghani/mkumar/releases/download/v$latestVersion/MKumar-v$latestVersion.apk"
    }

    fun getExternalStorageDir(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }
}