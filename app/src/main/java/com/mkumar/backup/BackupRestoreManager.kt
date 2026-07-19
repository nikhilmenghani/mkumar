package com.mkumar.backup

import android.content.Context
import com.mkumar.data.db.AppDatabase
import com.mkumar.data.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRestoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val provider: BackupProvider,
    private val snapshotter: BackupSnapshotter,
    private val database: AppDatabase,
    private val preferences: PreferencesManager
) {
    suspend fun findBackups(): List<RestoreOption> {
        val remote = provider.discoverBackup() ?: return emptyList()
        return remote.entries.map { RestoreOption(remote, it) }
    }

    suspend fun restore(option: RestoreOption): RestoreResult = withContext(Dispatchers.IO) {
        var download: File? = null
        var safety: DatabaseSnapshot? = null
        var databaseClosed = false
        val target = context.getDatabasePath("mkumar.db")
        try {
            download = File(context.cacheDir, "mkumar-restore-${System.currentTimeMillis()}.db")
            provider.download(option.remote, option.entry, download)
            val schema = snapshotter.validate(download, option.entry.sha256)
            require(schema == option.entry.databaseSchemaVersion) {
                "Backup schema does not match its manifest"
            }
            require(schema == 1) {
                "This app cannot restore database schema $schema"
            }

            safety = snapshotter.createSnapshot()
            database.close()
            databaseClosed = true

            target.parentFile?.mkdirs()
            File(target.path + "-wal").delete()
            File(target.path + "-shm").delete()
            Files.move(
                download.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
            download = null
            preferences.backupPrefs.lastBackupError = ""
            RestoreResult.Success(option.entry)
        } catch (t: Throwable) {
            if (databaseClosed && safety?.file?.isFile == true) {
                runCatching {
                    Files.copy(safety.file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    File(target.path + "-wal").delete()
                    File(target.path + "-shm").delete()
                }
            }
            RestoreResult.Failure(
                message = t.message ?: "Restore failed",
                cause = t,
                restartRequired = databaseClosed
            )
        } finally {
            download?.delete()
            safety?.file?.delete()
        }
    }
}
