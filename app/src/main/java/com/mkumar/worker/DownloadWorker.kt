package com.mkumar.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mkumar.network.ApkDownloadStrategy
import com.mkumar.network.DownloadStrategy
import com.mkumar.network.FileDownloadStrategy
import com.mkumar.notification.NotificationUtility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val DOWNLOAD_URL_KEY = "DOWNLOAD_URL"
        const val DEST_FILE_PATH_KEY = "DEST_FILE_PATH"
        const val DOWNLOAD_TYPE_KEY = "DOWNLOAD_TYPE"
        const val DOWNLOAD_TYPE_APK = "apk"
        const val DOWNLOAD_TYPE_FILE = "file"
        const val VERSION_KEY = "VERSION"
        const val PROGRESS_KEY = "DOWNLOAD_PROGRESS"
        const val OUTPUT_APK_PATH_KEY = "OUTPUT_APK_PATH"
    }

    override suspend fun doWork(): Result {
        val downloadUrl = inputData.getString(DOWNLOAD_URL_KEY)
        val destFilePath = inputData.getString(DEST_FILE_PATH_KEY)
        val downloadType = inputData.getString(DOWNLOAD_TYPE_KEY)
        val version = inputData.getString(VERSION_KEY).orEmpty().ifBlank { "update" }

        if (downloadUrl.isNullOrEmpty() || destFilePath.isNullOrEmpty()) {
            Log.e("MKumar-DownloadWorker", "Invalid input data: URL or destination path is missing.")
            return Result.failure()
        }

        // Instantiate the appropriate download strategy
        val downloadStrategy: DownloadStrategy = when (downloadType) {
            DOWNLOAD_TYPE_APK -> ApkDownloadStrategy()
            DOWNLOAD_TYPE_FILE -> FileDownloadStrategy()
            else -> {
                Log.e("MKumar-DownloadWorker", "Invalid download type specified.")
                return Result.failure()
            }
        }

        return withContext(Dispatchers.IO) {
            try {
                if (downloadType == DOWNLOAD_TYPE_APK) {
                    setForeground(downloadForegroundInfo(0, "Starting download"))
                }
                val downloadSuccess = if (downloadStrategy is ApkDownloadStrategy) {
                    downloadStrategy.downloadApk(downloadUrl, destFilePath) { fraction ->
                        val progress = (fraction * 100).toInt().coerceIn(0, 100)
                        setProgressAsync(workDataOf(PROGRESS_KEY to progress))
                        NotificationUtility.updateProgress(
                            applicationContext,
                            NotificationUtility.UPDATE_DOWNLOAD_NOTIFICATION_ID,
                            "Downloading MKumar $version",
                            "Downloading app update",
                            progress
                        )
                    }
                } else {
                    downloadStrategy.download(downloadUrl, destFilePath)
                }
                if (downloadSuccess) {
                    Log.d("MKumar-DownloadWorker", "Download successful: $destFilePath")
                    if (downloadType == DOWNLOAD_TYPE_APK) {
                        NotificationUtility.showUpdateReady(
                            applicationContext,
                            version,
                            destFilePath
                        )
                    }
                    Result.success(workDataOf(OUTPUT_APK_PATH_KEY to destFilePath))
                } else {
                    Log.e("MKumar-DownloadWorker", "Download failed.")
                    Result.retry()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MKumar-DownloadWorker", "Exception during download: ${e.message}")
                Result.retry()
            }
        }
    }

    private fun downloadForegroundInfo(progress: Int, text: String): ForegroundInfo =
        ForegroundInfo(
            NotificationUtility.UPDATE_DOWNLOAD_NOTIFICATION_ID,
            NotificationUtility.progressNotification(
                applicationContext,
                NotificationUtility.UPDATE_DOWNLOAD_NOTIFICATION_ID,
                "Downloading MKumar update",
                text,
                progress
            ),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
}
