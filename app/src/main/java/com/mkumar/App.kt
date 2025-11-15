package com.mkumar

import android.app.Application
import android.content.Context
import com.mkumar.data.PreferencesManager
import com.mkumar.data.SingleChoice
import com.mkumar.data.SingleSlider
import com.mkumar.data.SingleText
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {

    companion object {
        lateinit var appContext: Context

        val globalClass
            get() = appContext as App

        var hasRootAccess: Boolean = false
    }

    val preferencesManager: PreferencesManager by lazy { PreferencesManager() }
    val singleChoiceDialog: SingleChoice by lazy { SingleChoice }
    val singleTextDialog: SingleText by lazy { SingleText }
    val singleSliderDialog: SingleSlider by lazy { SingleSlider }

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}