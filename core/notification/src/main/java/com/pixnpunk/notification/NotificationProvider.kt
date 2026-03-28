package com.pixnpunk.notification

import androidx.work.ForegroundInfo
import java.util.*

interface NotificationProvider {
    fun createForegroundInfo(workId: UUID, title: String, progress: Float): ForegroundInfo
    fun showNotification(title: String, message: String)
}