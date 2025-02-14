package com.mkumar

import android.app.Application
import android.content.Context
import com.mkumar.data.PreferencesManager

class App: Application() {

    companion object {
        lateinit var appContext: Context

        val globalClass
            get() = appContext as App

        var hasRootAccess: Boolean = false
    }

    val preferencesManager: PreferencesManager by lazy { PreferencesManager() }

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}