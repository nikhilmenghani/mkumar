package com.mkumar

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.mkumar.data.SingleChoice
import com.mkumar.data.SingleSlider
import com.mkumar.data.SingleText
import com.mkumar.update.AppUpdateManager
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

        // Cancel incomplete legacy sync work that may survive an app upgrade. The old
        // pull worker treated records missing from GitHub as local deletions.
        WorkManager.getInstance(this).apply {
            cancelUniqueWork("mkumar_push_sync")
            cancelUniqueWork("mkumar_pull_sync")
            cancelAllWorkByTag("com.mkumar.sync.worker.PullFromCloudWorker")
            cancelAllWorkByTag("com.mkumar.sync.worker.SyncOutboxWorker")
        }
        AppUpdateManager.scheduleChecks(this)
    }

    // ✅ REQUIRED for WorkManager + Hilt (property-based API)
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
