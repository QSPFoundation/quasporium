package com.pixnpunk.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.*

class NotificationProviderImpl : NotificationProvider, KoinComponent {
    private val notificationManager = NotificationManagerCompat.from(get())

    companion object {
        const val CHANNEL_ID = "com.pixnpunk.quasporium.channel.download_channel"
        const val NOTIFICATION_ID = 300
    }

    init {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Game downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications about downloading and unpacking"
            }
        )
    }

    override fun createForegroundInfo(
        workId: UUID,
        title: String,
        progress: Float
    ) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ForegroundInfo(
            NOTIFICATION_ID,
            NotificationCompat.Builder(get(), CHANNEL_ID)
                .setContentTitle(title)
                .setTicker(title)
                .setContentText(
                    if (progress <= 0) "Setup..." else "${(progress * 100).toInt()}%"
                )
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true)
                .setProgress(
                    100,
                    (progress * 100).toInt(),
                    progress <= 0
                )
                .addAction(
                    android.R.drawable.ic_delete,
                    "Cancel",
                    get<WorkManager>().createCancelPendingIntent(workId)
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build(),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    } else {
        ForegroundInfo(
            NOTIFICATION_ID,
            NotificationCompat.Builder(get(), CHANNEL_ID)
                .setContentTitle(title)
                .setTicker(title)
                .setContentText(
                    if (progress <= 0) "Setup..." else "${(progress * 100).toInt()}%"
                )
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true)
                .setProgress(
                    100,
                    (progress * 100).toInt(),
                    progress <= 0
                )
                .addAction(
                    android.R.drawable.ic_delete,
                    "Cancel",
                    get<WorkManager>().createCancelPendingIntent(workId)
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun showNotification(title: String, message: String) =
        notificationManager.notify(
            (System.currentTimeMillis() % 10000).toInt(),
            NotificationCompat.Builder(get(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
        )
}