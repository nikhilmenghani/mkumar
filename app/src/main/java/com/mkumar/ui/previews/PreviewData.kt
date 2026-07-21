package com.mkumar.ui.previews

import com.mkumar.backup.BackupEntry
import com.mkumar.backup.BackupManifest
import com.mkumar.backup.RemoteBackup
import com.mkumar.backup.RestoreOption
import com.mkumar.model.OrderRowUi
import com.mkumar.model.OrderWithCustomerInfo
import com.mkumar.model.UiCustomerMini
import java.time.Instant

internal object PreviewData {
    private val now = System.currentTimeMillis()

    val orders = listOf(
        OrderWithCustomerInfo("o1", 210, now - 3_600_000, now - 900_000, 2_000, 0, 1_000, "c1", "Aarav Sharma", "9876543210"),
        OrderWithCustomerInfo("o2", 209, now - 86_400_000, now - 7_200_000, 3_750, 3_500, 0, "c2", "Meera Patel", "9988776655"),
        OrderWithCustomerInfo("o3", 208, now - 172_800_000, now - 90_000_000, 1_450, 0, 450, "c3", "Kabir Singh", "9123456780"),
        OrderWithCustomerInfo("o4", 207, now - 259_200_000, now - 180_000_000, 5_200, 4_900, 0, "c4", "Ananya Gupta", "9012345678")
    )

    val customers = listOf(
        UiCustomerMini("c1", "Aarav Sharma", "9876543210"),
        UiCustomerMini("c2", "Meera Patel", "9988776655"),
        UiCustomerMini("c3", "Kabir Singh", "9123456780"),
        UiCustomerMini("c4", "Ananya Gupta", "9012345678"),
        UiCustomerMini("c5", "Rohan Verma", "9000012345")
    )

    val orderRows = listOf(
        OrderRowUi("o1", now - 3_600_000, now - 900_000, "MKumar-210", 2_000, 1_000),
        OrderRowUi("o2", now - 86_400_000, now - 7_200_000, "MKumar-209", 3_750, 0, 3_500),
        OrderRowUi("o3", now - 172_800_000, now - 90_000_000, "MKumar-208", 1_450, 450)
    )

    val backups = listOf(4, 28, 52).mapIndexed { index, hoursAgo ->
        val entry = BackupEntry(
            databaseSchemaVersion = 12,
            appVersionCode = 1,
            createdAtUtc = Instant.now().minusSeconds(hoursAgo * 3_600L).toString(),
            backupPath = "backups/com.mkumar/snapshots/device-$index/preview-$index.db",
            sizeBytes = 2_400_000L + index * 350_000L,
            sha256 = "preview-sha-$index",
            trigger = if (index == 0) "MANUAL" else "SCHEDULED",
            deviceId = "8f72a1c${index}9-preview-device-id",
            deviceName = if (index == 0) "Galaxy S26 Ultra" else "Pixel Test Device"
        )
        RestoreOption(
            remote = RemoteBackup(
                owner = "owner",
                repository = "backups",
                branch = "main",
                manifest = BackupManifest(backups = listOf(entry))
            ),
            entry = entry
        )
    }
}
