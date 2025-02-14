package com.mkumar.ui.screens

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.mkumar.MainActivity
import com.mkumar.common.AppConstants.getAppDownloadUrl
import com.mkumar.common.AppConstants.getExternalStorageDir
import com.mkumar.common.PackageManager.getCurrentVersion
import com.mkumar.common.PackageManager.installApk
import com.mkumar.common.navigateWithState
import com.mkumar.network.VersionFetcher.fetchLatestVersion
import com.mkumar.ui.navigation.Screens
import com.mkumar.worker.DownloadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalActivity.current as MainActivity
    val workManager = WorkManager.getInstance(context)
    val currentVersion by remember { mutableStateOf(getCurrentVersion(context)) }
    var latestVersion by remember { mutableStateOf(currentVersion) }
    var isLatestVersion by remember { mutableStateOf(true) }
    var isDownloading by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            latestVersion = fetchLatestVersion()
            isLatestVersion = (currentVersion == latestVersion) || (latestVersion == "Unknown")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "MKumar") },
                actions = {
                    IconButton(onClick = {
                        // Restart the activity to apply the new theme
                        context.restartActivity()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = {
                        navController.navigateWithState(
                            route = Screens.Settings.name
                        )
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isLatestVersion) {
                FloatingActionButton(
                    onClick = {
                        isDownloading = true

                        val downloadUrl = getAppDownloadUrl(latestVersion)
                        val destFilePath =
                            "${getExternalStorageDir()}/Download/MKumar.apk"

                        val inputData = workDataOf(
                            DownloadWorker.DOWNLOAD_URL_KEY to downloadUrl,
                            DownloadWorker.DEST_FILE_PATH_KEY to destFilePath,
                            DownloadWorker.DOWNLOAD_TYPE_KEY to DownloadWorker.DOWNLOAD_TYPE_APK
                        )

                        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                            .setInputData(inputData)
                            .setConstraints(
                                Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build()
                            )
                            .build()

                        // Enqueue the download request using WorkManager
                        workManager.enqueue(downloadRequest)

                        // Observe the WorkManager status
                        workManager.getWorkInfoByIdLiveData(downloadRequest.id)
                            .observeForever { info ->
                                if (info?.state == WorkInfo.State.SUCCEEDED) {
                                    isDownloading = false
                                    if (context.packageManager.canRequestPackageInstalls()) {
                                        installApk(context, destFilePath)
                                    }
                                } else if (info?.state == WorkInfo.State.FAILED) {
                                    isDownloading = false
                                    Toast.makeText(
                                        context,
                                        "Failed to download update",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    },
                    shape = androidx.compose.foundation.shape.CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Update Available\nMKumar v$latestVersion",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        },
        content = { paddingValues ->
            Column {
                Row(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                }
                HorizontalDivider()
            }
        }
    )
}