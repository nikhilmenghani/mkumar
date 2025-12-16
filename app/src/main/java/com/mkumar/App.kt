package com.mkumar

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mkumar.data.SingleChoice
import com.mkumar.data.SingleSlider
import com.mkumar.data.SingleText
import com.mkumar.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    companion object {
        lateinit var appContext: Context
        val globalClass get() = appContext as App
        var hasRootAccess: Boolean = false
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    val singleChoiceDialog: SingleChoice by lazy { SingleChoice }
    val singleTextDialog: SingleText by lazy { SingleText }
    val singleSliderDialog: SingleSlider by lazy { SingleSlider }

    override fun onCreate() {
        super.onCreate()
        appContext = this

        // Optional periodic pull
        SyncScheduler.schedulePeriodicPull(this)
    }

    // ✅ REQUIRED for WorkManager + Hilt (property-based API)
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
