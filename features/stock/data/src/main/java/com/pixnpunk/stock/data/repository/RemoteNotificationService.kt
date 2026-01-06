package com.pixnpunk.stock.data.repository

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.pixnpunk.dto.DownloadStatus
import com.pixnpunk.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RemoteNotificationService : Service() {

    private val repository: RemoteRepositoryImpl by inject()
    private val notificationHelper: NotificationHelper by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ServiceCompat.startForeground(
            this,
            NotificationHelper.NOTIFICATION_ID,
            notificationHelper.getBaseNotification().setContentTitle("Setup...").build(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }
        )

        serviceScope.launch {
            repository.progressDownloadFlow.collect { state ->
                if (state.status == DownloadStatus.NOT_STARTED) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    notificationHelper.updateNotification(state)
                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}