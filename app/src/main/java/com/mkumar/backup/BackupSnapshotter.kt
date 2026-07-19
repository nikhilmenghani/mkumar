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

            try {
                // Some newer Android SQLite builds validate FTS3/FTS4 indexes through a
                // write-like virtual-table command. The file is a disposable snapshot (or
                // restore download), not the live Room database, so allow that validation.
                val sqlite = android.database.sqlite.SQLiteDatabase.openDatabase(
                    file.absolutePath,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
                )
                sqlite.use { db ->
                    // Newer SQLite builds invoke virtual-table xIntegrity hooks for both
                    // integrity_check and quick_check. Check every physical table (including
                    // FTS shadow tables) individually so the read-only pragma path does not
                    // invoke the FTS wrapper itself.
                    checkPhysicalTable(db, "sqlite_schema")
                    val physicalTables = db.rawQuery(
                        "SELECT name, sql FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'",
                        null
                    ).use { cursor ->
                        buildList {
                            while (cursor.moveToNext()) {
                                val name = cursor.getString(0)
                                val sql = if (cursor.isNull(1)) "" else cursor.getString(1)
                                if (!sql.trimStart().startsWith("CREATE VIRTUAL TABLE", ignoreCase = true)) {
                                    add(name)
                                }
                            }
                        }
                    }
                    physicalTables.forEach { checkPhysicalTable(db, it) }

                    // Validate FTS inverted indexes explicitly through write-classified SQL.
                    // These special INSERT commands only perform validation; they do not add rows.
                    db.execSQL("INSERT INTO customer_fts(customer_fts) VALUES('integrity-check')")
                    db.execSQL("INSERT INTO order_fts(order_fts) VALUES('integrity-check')")

                    db.rawQuery("PRAGMA user_version", null).use { cursor ->
                        if (cursor.moveToFirst()) cursor.getInt(0) else 0
                    }
                }
            } finally {
                File(file.path + "-wal").delete()
                File(file.path + "-shm").delete()
                File(file.path + "-journal").delete()
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

    private fun checkPhysicalTable(
        database: android.database.sqlite.SQLiteDatabase,
        tableName: String
    ) {
        val quotedName = tableName.replace("'", "''")
        val result = database.rawQuery("PRAGMA integrity_check('$quotedName')", null).use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else "failed"
        }
        require(result.equals("ok", ignoreCase = true)) {
            "SQLite integrity check failed for $tableName: $result"
        }
    }
}
