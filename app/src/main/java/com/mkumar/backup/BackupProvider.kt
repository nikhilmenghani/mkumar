package com.mkumar.backup

import java.io.File

interface BackupProvider {
    suspend fun discoverBackup(): RemoteBackup?
    suspend fun upload(snapshot: File, manifest: BackupManifest): RemoteBackup
    suspend fun download(backup: RemoteBackup, destination: File)
}
