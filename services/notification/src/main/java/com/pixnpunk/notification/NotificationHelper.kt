package com.pixnpunk.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.pixnpunk.dto.DownloadStatus
import com.pixnpunk.dto.ProgressState

class NotificationHelper(
    private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "com.pixnpunk.quasporium.channel.download_channel"
        const val NOTIFICATION_ID = 300
    }

    init {
        createChannel()
    }

    fun getBaseNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

    fun updateNotification(state: ProgressState) {
        if (state.status == DownloadStatus.NOT_STARTED) {
            cancelNotification()
            return
        }

        notificationManager.notify(
            NOTIFICATION_ID,
            getBaseNotification().apply {
                when (state.status) {
                    DownloadStatus.DOWNLOAD_FILE -> {
                        setContentTitle("Download file...")
                        setProgress(100, state.progress.toInt(), false)
                        setContentText("ETA: ${state.time}")
                    }

                    DownloadStatus.WRITE_FILE -> {
                        setContentTitle("Write...")
                        setProgress(0, 0, true)
                    }

                    DownloadStatus.UNPACK_FILE -> {
                        setContentTitle("Unpack...")
                        setProgress(0, 0, true)
                    }

                    else -> {}
                }
            }.build()
        )
    }

    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Game Downloads",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}