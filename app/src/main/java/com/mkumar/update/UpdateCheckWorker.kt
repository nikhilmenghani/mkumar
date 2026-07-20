package com.mkumar.update

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mkumar.common.manager.PackageManager.getCurrentVersion
import com.mkumar.common.version.isVersionNewer
import com.mkumar.network.VersionFetcher
import com.mkumar.notification.NotificationUtility
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UpdateCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val versionFetcher: VersionFetcher
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val release = versionFetcher.fetchLatestRelease() ?: return Result.retry()
        val currentVersion = getCurrentVersion(applicationContext)
        if (!isVersionNewer(release.version, currentVersion)) return Result.success()

        NotificationUtility.showUpdateAvailable(
            applicationContext,
            release.version,
            release.downloadUrl
        )
        return Result.success()
    }
}
