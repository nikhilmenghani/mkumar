package com.mkumar.viewmodel

data class HomeUiState(
    val currentVersion: String = "",
    val latestVersion: String = "",
    val isLatest: Boolean = true,

    // version check
    val isChecking: Boolean = false,

    // apk download
    val isDownloading: Boolean = false,

    // guard to avoid re-running bootstrap
    val hasBootstrapped: Boolean = false
)
