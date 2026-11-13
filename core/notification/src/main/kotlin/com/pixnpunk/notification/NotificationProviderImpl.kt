package com.pixnpunk.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import java.util.*

class NotificationProviderImpl(
    private val context: Context,
    private val workManager: WorkManager
) : NotificationProvider {
    private val notificationManager = NotificationManagerCompat.from(context)

    private companion object {
        const val CHANNEL_ID = "com.pixnpunk.quasporium.channel.download_channel"
        const val NOTIFICATION_ID = 300
        const val MAX_PROGRESS = 100
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

    private val UUID.notificationId: Int
        get() = hashCode()

    private fun progressNotification(
        workId: UUID,
        title: String,
        text: String,
        progress: Float,
        ongoing: Boolean
    ) = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(title)
        .setTicker(title)
        .setContentText(text)
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setOngoing(ongoing)
        .setOnlyAlertOnce(true)
        .setProgress(
            MAX_PROGRESS,
            (progress * MAX_PROGRESS).toInt(),
            ongoing && progress <= 0
        )
        .addAction(
            android.R.drawable.ic_delete,
            "Cancel",
            workManager.createCancelPendingIntent(workId)
        )
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    @SuppressLint("MissingPermission")
    private fun notify(workId: UUID, notification: Notification) =
        notificationManager.notify(workId.notificationId, notification)

    override fun createForegroundInfo(
        workId: UUID,
        title: String,
        text: String,
        progress: Float
    ): ForegroundInfo {
        val notification = progressNotification(workId, title, text, progress, ongoing = true)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ForegroundInfo(
                NOTIFICATION_ID, notification,
                FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        else ForegroundInfo(NOTIFICATION_ID, notification)
    }

    override fun showPaused(workId: UUID, title: String, text: String, progress: Float) =
        notify(
            workId,
            progressNotification(workId, title, text, progress, ongoing = false)
        )

    override fun showResult(workId: UUID, title: String, text: String) =
        notify(
            workId,
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
        )

    override fun cancel(workId: UUID) = notificationManager.cancel(workId.notificationId)
}
