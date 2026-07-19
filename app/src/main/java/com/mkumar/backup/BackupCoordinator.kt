package com.mkumar.backup

import android.content.Context
import com.mkumar.data.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val snapshotter: BackupSnapshotter,
    private val provider: BackupProvider,
    private val preferences: PreferencesManager
) {
    suspend fun backup(trigger: BackupTrigger): BackupResult = withContext(Dispatchers.IO) {
        var snapshot: DatabaseSnapshot? = null
        try {
            snapshot = snapshotter.createSnapshot()
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val manifest = BackupManifest(
                databaseSchemaVersion = snapshot.schemaVersion,
                appVersionCode = packageInfo.longVersionCode,
                createdAtUtc = Instant.now().toString(),
                backupPath = "backups/latest.db",
                sizeBytes = snapshot.file.length(),
                sha256 = snapshot.sha256,
                trigger = trigger.name
            )
            provider.upload(snapshot.file, manifest)
            preferences.backupPrefs.lastSuccessfulBackupAt = manifest.createdAtUtc
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
}
