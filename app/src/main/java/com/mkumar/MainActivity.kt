package com.mkumar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.core.app.NotificationManagerCompat
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mkumar.data.PreferencesManager
import com.mkumar.backup.BackupScheduler
import com.mkumar.permission.Permissions
import com.mkumar.ui.navigation.ScreenNavigator
import com.mkumar.ui.screens.PermissionsScreen
import com.mkumar.ui.theme.LocalPreferencesManager
import com.mkumar.ui.theme.MKumarTheme
import com.mkumar.ui.theme.NikTheme
import com.mkumar.viewmodel.CustomerViewModel
import com.mkumar.common.manager.PackageManager.installApk
import com.mkumar.update.AppUpdateManager
import com.mkumar.update.UpdateActionReceiver
import com.mkumar.worker.DownloadWorker
import com.mkumar.notification.NotificationUtility
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var backupScheduler: BackupScheduler

    private val customerViewModel: CustomerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backupScheduler.schedulePeriodic()
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(
                LocalPreferencesManager provides preferencesManager
            ) {
                NikTheme {
                    if (Build.VERSION.RELEASE.toInt() <= 13 || Permissions.hasAllRequiredPermissions(this)) {
                        ScreenNavigator(customerViewModel)
                    } else {
                        PermissionsScreen(
                            onAllPermissionsGranted = ::onAllPermissionsGranted
                        )
                    }
                }
            }
        }
        handleUpdateIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleUpdateIntent(intent)
    }

    private fun handleUpdateIntent(updateIntent: Intent) {
        when (updateIntent.action) {
            UpdateActionReceiver.ACTION_DOWNLOAD_UPDATE -> {
                val version = updateIntent
                    .getStringExtra(UpdateActionReceiver.EXTRA_VERSION)
                    .orEmpty()
                val url = updateIntent
                    .getStringExtra(UpdateActionReceiver.EXTRA_DOWNLOAD_URL)
                    .orEmpty()
                if (version.isNotBlank() && url.isNotBlank()) {
                    val workId = AppUpdateManager.enqueueDownload(this, version, url)
                    var installerLaunched = false
                    WorkManager.getInstance(this)
                        .getWorkInfoByIdLiveData(workId)
                        .observe(this) { info ->
                            if (!installerLaunched && info?.state == WorkInfo.State.SUCCEEDED) {
                                installerLaunched = true
                                info.outputData
                                    .getString(DownloadWorker.OUTPUT_APK_PATH_KEY)
                                    ?.let(::launchUpdateInstaller)
                            }
                        }
                }
            }
            UpdateActionReceiver.ACTION_INSTALL_UPDATE -> {
                updateIntent.getStringExtra(UpdateActionReceiver.EXTRA_APK_PATH)
                    ?.takeIf(String::isNotBlank)
                    ?.let(::launchUpdateInstaller)
            }
        }
        // Prevent configuration changes from replaying the update action.
        updateIntent.action = null
    }

    private fun launchUpdateInstaller(apkPath: String) {
        NotificationManagerCompat.from(this).apply {
            cancel(NotificationUtility.UPDATE_AVAILABLE_NOTIFICATION_ID)
            cancel(NotificationUtility.UPDATE_DOWNLOAD_NOTIFICATION_ID)
            cancel(NotificationUtility.UPDATE_READY_NOTIFICATION_ID)
        }
        installApk(this, apkPath)
    }

    private fun onAllPermissionsGranted() {
        if (Permissions.hasAllRequiredPermissions(this)) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    fun restartActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun restartApplicationAfterRestore() {
        val intent = Intent(this, RestartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }
        startActivity(intent)
        Handler(Looper.getMainLooper()).postDelayed({
            finishAffinity()
            Process.killProcess(Process.myPid())
        }, 200)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MKumarTheme {
        Greeting("Android")
    }
}
