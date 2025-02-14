package com.mkumar.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mkumar.network.ApkDownloadStrategy
import com.mkumar.network.DownloadStrategy
import com.mkumar.network.FileDownloadStrategy
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
    }

    override suspend fun doWork(): Result {
        val downloadUrl = inputData.getString(DOWNLOAD_URL_KEY)
        val destFilePath = inputData.getString(DEST_FILE_PATH_KEY)
        val downloadType = inputData.getString(DOWNLOAD_TYPE_KEY)

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
                val downloadSuccess = downloadStrategy.download(downloadUrl, destFilePath)
                if (downloadSuccess) {
                    Log.d("MKumar-DownloadWorker", "Download successful: $destFilePath")
                    Result.success()
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
}