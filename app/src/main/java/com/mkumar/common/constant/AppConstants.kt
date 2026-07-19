package com.mkumar.common.constant

import android.os.Environment

object AppConstants {

    const val latestVersionUrl = "https://api.github.com/repos/nikhilmenghani/mkumar/releases/latest"

    const val emptyString = ""

    fun getExternalStorageDir(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }
}
