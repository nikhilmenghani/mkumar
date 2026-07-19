package com.mkumar.backup

import android.content.Context
import android.os.Build
import com.mkumar.data.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val snapshotter: BackupSnapshotter,
    private val provider: BackupProvider,
    private val preferences: PreferencesManager
) {
    suspend fun backup(
        trigger: BackupTrigger,
        onProgress: suspend (stage: String, percent: Int) -> Unit = { _, _ -> }
    ): BackupResult = withContext(Dispatchers.IO) {
        var snapshot: DatabaseSnapshot? = null
        try {
            onProgress("Preparing database snapshot", 10)
            snapshot = snapshotter.createSnapshot()
            onProgress("Validating database snapshot", 35)
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val createdAt = Instant.now()
            val deviceId = preferences.backupPrefs.deviceId.ifBlank {
                UUID.randomUUID().toString().also { preferences.backupPrefs.deviceId = it }
            }
            val deviceName = preferences.backupPrefs.deviceName.ifBlank {
                "${Build.MANUFACTURER} ${Build.MODEL}".trim().also {
                    preferences.backupPrefs.deviceName = it
                }
            }
            val entry = BackupEntry(
                databaseSchemaVersion = snapshot.schemaVersion,
                appVersionCode = packageInfo.longVersionCode,
                createdAtUtc = createdAt.toString(),
                backupPath = "backups/snapshots/$deviceId/${SNAPSHOT_NAME_FORMAT.format(createdAt)}.db",
                sizeBytes = snapshot.file.length(),
                sha256 = snapshot.sha256,
                trigger = trigger.name,
                deviceId = deviceId,
                deviceName = deviceName
            )
            val manifest = BackupManifest(backups = listOf(entry))
            onProgress("Uploading backup to GitHub", 55)
            provider.upload(snapshot.file, manifest)
            onProgress("Finalizing backup", 90)
            preferences.backupPrefs.lastSuccessfulBackupAt = entry.createdAtUtc
            preferences.backupPrefs.lastBackupError = ""
            BackupResult.Success(manifest)
        } catch (t: Throwable) {
            val message = t.message ?: "Backup failed"
            preferences.backupPrefs.lastBackupError = message
            BackupResult.Failure(message, t)
        } finally {
            snapshot?.file?.delete()
        }
    }

    companion object {
        private val SNAPSHOT_NAME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssSSS'Z'").withZone(ZoneOffset.UTC)
    }
}
