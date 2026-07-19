package com.mkumar.backup

import org.junit.Assert.assertEquals
import org.junit.Test

class BackupManifestTest {
    @Test
    fun availableBackups_sortsNewestFirstAndRemoteCapsAtThree() {
        val entries = (1..4).map { day -> entry("2026-07-0${day}T12:00:00Z") }
        val remote = RemoteBackup("owner", "repo", "main", BackupManifest(backups = entries))

        assertEquals(3, remote.entries.size)
        assertEquals("2026-07-04T12:00:00Z", remote.entries.first().createdAtUtc)
        assertEquals("2026-07-02T12:00:00Z", remote.entries.last().createdAtUtc)
    }

    @Test
    fun availableBackups_readsLegacyManifest() {
        val manifest = BackupManifest(
            formatVersion = 1,
            databaseSchemaVersion = 1,
            appVersionCode = 10,
            createdAtUtc = "2026-07-01T12:00:00Z",
            backupPath = "backups/latest.db",
            sizeBytes = 42,
            sha256 = "abc",
            trigger = "MANUAL"
        )

        assertEquals("backups/latest.db", manifest.availableBackups().single().backupPath)
    }

    private fun entry(createdAt: String) = BackupEntry(
        databaseSchemaVersion = 1,
        appVersionCode = 1,
        createdAtUtc = createdAt,
        backupPath = "$createdAt.db",
        sizeBytes = 1,
        sha256 = createdAt,
        trigger = "SCHEDULED"
    )
}
