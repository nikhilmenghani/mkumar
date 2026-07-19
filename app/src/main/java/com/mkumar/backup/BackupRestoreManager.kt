package com.mkumar.backup

import android.content.Context
import com.mkumar.data.db.AppDatabase
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
    private val database: AppDatabase
) {
    suspend fun findBackup(): RemoteBackup? = provider.discoverBackup()

    suspend fun restoreLatest(): RestoreResult = withContext(Dispatchers.IO) {
        var download: File? = null
        var safety: DatabaseSnapshot? = null
        var databaseClosed = false
        val target = context.getDatabasePath("mkumar.db")
        try {
            val remote = provider.discoverBackup()
                ?: return@withContext RestoreResult.Failure("No M Kumar backup was found for this token")
            download = File(context.cacheDir, "mkumar-restore-${System.currentTimeMillis()}.db")
            provider.download(remote, download)
            val schema = snapshotter.validate(download, remote.manifest.sha256)
            require(schema == remote.manifest.databaseSchemaVersion) {
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
            RestoreResult.Success(remote.manifest)
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
