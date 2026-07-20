package com.mkumar.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mkumar.App
import com.mkumar.R
import com.mkumar.update.UpdateActionReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object NotificationUtility {

    const val CHANNEL_ID = "progress_channel_id"
    const val NOTIFICATION_ID = 1
    const val BACKUP_NOTIFICATION_ID = 2101
    const val UPDATE_DOWNLOAD_NOTIFICATION_ID = 2201
    const val UPDATE_AVAILABLE_NOTIFICATION_ID = 2202
    const val UPDATE_READY_NOTIFICATION_ID = 2203
    private const val UPDATE_CHANNEL_ID = "app_updates"

    fun startFileDownload(context: Context) {
        createNotificationChannel(context)

        val totalProgress = 100

        // Simulate a download with a coroutine (replace with actual logic)
        CoroutineScope(Dispatchers.IO).launch {
            for (progress in 0..totalProgress step 10) {
                delay(500) // Simulate download delay
                showProgressNotification(context, progress)
            }
        }
    }

    @SuppressLint("MissingPermission", "NotificationPermission")
    fun showProgressNotification(
        context: Context = App.globalClass,
        progress: Int,
        progressText: String = "Download in progress",
        channelId: String = CHANNEL_ID,
        contentTitle: String = "File Download",
        priority: Int = NotificationCompat.PRIORITY_HIGH,
        completeText: String = "Download complete",
        notificationId: Int = NOTIFICATION_ID
    ) {
        val notificationManager = NotificationManagerCompat.from(context)

        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(contentTitle)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(priority)
            .setOnlyAlertOnce(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Ensures sound, vibration, etc.
            .setCategory(NotificationCompat.CATEGORY_PROGRESS) // Explicitly set category

        if (progress < 100) {
            builder.setContentText("$progressText: $progress%")
                .setProgress(100, progress, false)
        } else {
            notificationManager.cancel(notificationId)
            builder.setContentText(completeText)
                .setProgress(0, 0, false)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
        }
        notificationManager.notify(notificationId, builder.build())
    }


    fun createNotificationChannel(
        context: Context = App.globalClass,
        name: String = "Progress Channel",
        descriptionText: String = "Notification channel for progress updates",
        importance: Int = NotificationManager.IMPORTANCE_HIGH,
        channelId: String = CHANNEL_ID
    ) {
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
            setSound(null, null)
            enableLights(true)
            lightColor = android.graphics.Color.BLUE
            enableVibration(true)
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun progressNotification(
        context: Context,
        notificationId: Int,
        title: String,
        text: String,
        progress: Int
    ): Notification {
        createNotificationChannel(
            context = context,
            name = "Background progress",
            descriptionText = "Backup and app-update progress",
            importance = NotificationManager.IMPORTANCE_LOW
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle(title)
            .setContentText(text)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOnlyAlertOnce(true)
            .setOngoing(progress < 100)
            .setProgress(100, progress.coerceIn(0, 100), false)
            .build()
    }

    @SuppressLint("MissingPermission")
    fun updateProgress(
        context: Context,
        notificationId: Int,
        title: String,
        text: String,
        progress: Int
    ) {
        NotificationManagerCompat.from(context).notify(
            notificationId,
            progressNotification(context, notificationId, title, text, progress)
        )
    }

    @SuppressLint("MissingPermission")
    fun showUpdateAvailable(context: Context, version: String, downloadUrl: String) {
        createUpdateChannel(context)
        val intent = Intent(context, UpdateActionReceiver::class.java).apply {
            action = UpdateActionReceiver.ACTION_DOWNLOAD_UPDATE
            putExtra(UpdateActionReceiver.EXTRA_VERSION, version)
            putExtra(UpdateActionReceiver.EXTRA_DOWNLOAD_URL, downloadUrl)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            version.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("MKumar $version is available")
            .setContentText("Tap to download the update")
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.stat_sys_download, "Download", pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context)
            .notify(UPDATE_AVAILABLE_NOTIFICATION_ID, notification)
    }

    @SuppressLint("MissingPermission")
    fun showUpdateReady(context: Context, version: String, apkPath: String) {
        createUpdateChannel(context)
        val intent = Intent(context, UpdateActionReceiver::class.java).apply {
            action = UpdateActionReceiver.ACTION_INSTALL_UPDATE
            putExtra(UpdateActionReceiver.EXTRA_APK_PATH, apkPath)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            apkPath.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("MKumar $version is ready")
            .setContentText("Tap to review and install the update")
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.stat_sys_download_done, "Install", pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).apply {
            cancel(UPDATE_DOWNLOAD_NOTIFICATION_ID)
            notify(UPDATE_READY_NOTIFICATION_ID, notification)
        }
    }

    private fun createUpdateChannel(context: Context) {
        createNotificationChannel(
            context = context,
            name = "App updates",
            descriptionText = "New MKumar versions and installation status",
            importance = NotificationManager.IMPORTANCE_DEFAULT,
            channelId = UPDATE_CHANNEL_ID
        )
    }
}

