package com.mkumar.update

import android.content.Context
import android.os.Environment
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.mkumar.worker.DownloadWorker
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

object AppUpdateManager {
    private const val PERIODIC_CHECK_WORK = "mkumar_periodic_update_check"
    private const val STARTUP_CHECK_WORK = "mkumar_startup_update_check"
    private const val UPDATE_DOWNLOAD_WORK = "mkumar_apk_update_download"

    fun scheduleChecks(context: Context, intervalHours: Int) {
        if (intervalHours <= 0) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_CHECK_WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
            intervalHours.toLong(),
            TimeUnit.HOURS
        )
            .setConstraints(networkConstraints())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_CHECK_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun checkOnAppStart(context: Context) {
        val request = OneTimeWorkRequestBuilder<UpdateCheckWorker>()
            .setConstraints(networkConstraints())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            STARTUP_CHECK_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun enqueueDownload(context: Context, version: String, downloadUrl: String): UUID {
        val downloadDir = checkNotNull(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS))
        val destination = File(downloadDir, "MKumar-$version.apk")
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(
                workDataOf(
                    DownloadWorker.DOWNLOAD_URL_KEY to downloadUrl,
                    DownloadWorker.DEST_FILE_PATH_KEY to destination.absolutePath,
                    DownloadWorker.DOWNLOAD_TYPE_KEY to DownloadWorker.DOWNLOAD_TYPE_APK,
                    DownloadWorker.VERSION_KEY to version
                )
            )
            .setConstraints(networkConstraints())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            UPDATE_DOWNLOAD_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
        return request.id
    }

    private fun networkConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
}
