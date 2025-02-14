package com.mkumar.network

interface DownloadStrategy {
    suspend fun download(downloadUrl: String, destFilePath: String): Boolean
}