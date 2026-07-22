package com.mkumar.backup.github

import com.mkumar.backup.BackupEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRetentionTest {
    @Test
    fun `retention is applied only to the uploading device`() {
        val deviceA = (1..4).map { entry("a$it", "device-a", "2026-07-0$it") }
        val deviceB = (1..6).map { entry("b$it", "device-b", "2026-06-0$it") }
        val newestA = entry("a5", "device-a", "2026-07-05")

        val retained = retainBackupsPerDevice(deviceA + deviceB, newestA, retentionCount = 3)

        assertEquals(3, retained.count { it.deviceId == "device-a" })
        assertEquals(6, retained.count { it.deviceId == "device-b" })
        assertEquals(9, retained.size)
        assertTrue(retained.any { it.backupPath == "a5" })
    }

    private fun entry(path: String, deviceId: String, createdAt: String) = BackupEntry(
        databaseSchemaVersion = 1,
        appVersionCode = 1,
        createdAtUtc = createdAt,
        backupPath = path,
        sizeBytes = 1,
        sha256 = path,
        trigger = "MANUAL",
        deviceId = deviceId,
        deviceName = deviceId
    )
}
