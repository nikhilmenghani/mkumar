package com.mkumar.backup

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mkumar.data.db.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

interface BackupSnapshotter {
    suspend fun createSnapshot(): DatabaseSnapshot
    suspend fun validate(file: File, expectedSha256: String? = null): Int
}

@Singleton
class RoomBackupSnapshotter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) : BackupSnapshotter {

    override suspend fun createSnapshot(): DatabaseSnapshot = withContext(Dispatchers.IO) {
        val output = File(context.cacheDir, "mkumar-backup-${System.currentTimeMillis()}.db")
        if (output.exists() && !output.delete()) error("Could not clear old backup snapshot")

        val escapedPath = output.absolutePath.replace("'", "''")
        database.openHelper.writableDatabase.execSQL("VACUUM INTO '$escapedPath'")
        val version = validate(output)
        DatabaseSnapshot(output, version, sha256(output))
    }

    override suspend fun validate(file: File, expectedSha256: String?): Int =
        withContext(Dispatchers.IO) {
            require(file.isFile && file.length() > 0) { "Backup database is empty" }
            if (expectedSha256 != null) {
                require(sha256(file).equals(expectedSha256, ignoreCase = true)) {
                    "Backup checksum does not match the manifest"
                }
            }

            val sqlite = android.database.sqlite.SQLiteDatabase.openDatabase(
                file.absolutePath,
                null,
                android.database.sqlite.SQLiteDatabase.OPEN_READONLY
            )
            sqlite.use { db ->
                val integrity = db.rawQuery("PRAGMA integrity_check", null).use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else "failed"
                }
                require(integrity.equals("ok", ignoreCase = true)) {
                    "SQLite integrity check failed: $integrity"
                }
                db.rawQuery("PRAGMA user_version", null).use { cursor ->
                    if (cursor.moveToFirst()) cursor.getInt(0) else 0
                }
            }
        }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
