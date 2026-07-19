package com.mkumar.backup

import kotlinx.serialization.Serializable

enum class BackupTrigger {
    SCHEDULED,
    MANUAL,
    ORDER_COMPLETED
}

@Serializable
data class BackupManifest(
    val formatVersion: Int = 2,
    val applicationId: String = "com.mkumar",
    val databaseName: String = "mkumar.db",
    val backups: List<BackupEntry> = emptyList(),
    // Version-1 fields retained so repositories created by the first implementation remain restorable.
    val databaseSchemaVersion: Int = 0,
    val appVersionCode: Long = 0,
    val createdAtUtc: String = "",
    val backupPath: String = "",
    val sizeBytes: Long = 0,
    val sha256: String = "",
    val trigger: String = ""
) {
    fun availableBackups(): List<BackupEntry> =
        if (backups.isNotEmpty()) backups.sortedByDescending { it.createdAtUtc }
        else if (backupPath.isNotBlank()) listOf(
            BackupEntry(
                databaseSchemaVersion = databaseSchemaVersion,
                appVersionCode = appVersionCode,
                createdAtUtc = createdAtUtc,
                backupPath = backupPath,
                sizeBytes = sizeBytes,
                sha256 = sha256,
                trigger = trigger
            )
        ) else emptyList()
}

@Serializable
data class BackupEntry(
    val databaseSchemaVersion: Int,
    val appVersionCode: Long,
    val createdAtUtc: String,
    val backupPath: String,
    val sizeBytes: Long,
    val sha256: String,
    val trigger: String,
    val deviceId: String = "",
    val deviceName: String = "Unknown device"
)

data class DatabaseSnapshot(
    val file: java.io.File,
    val schemaVersion: Int,
    val sha256: String,
    val capturedAtUtc: java.time.Instant
)

data class RemoteBackup(
    val owner: String,
    val repository: String,
    val branch: String,
    val manifest: BackupManifest
) {
    val entries: List<BackupEntry> get() = manifest.availableBackups()
}

data class RestoreOption(
    val remote: RemoteBackup,
    val entry: BackupEntry
)

sealed interface BackupResult {
    data class Success(val manifest: BackupManifest) : BackupResult
    data class Failure(val message: String, val cause: Throwable? = null) : BackupResult
}

sealed interface RestoreResult {
    data class Success(val entry: BackupEntry) : RestoreResult
    data class Failure(
        val message: String,
        val cause: Throwable? = null,
        val restartRequired: Boolean = false
    ) : RestoreResult
}
