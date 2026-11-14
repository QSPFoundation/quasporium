package com.pixnpunk.notification

import android.app.PendingIntent
import androidx.work.ForegroundInfo
import java.util.*

interface NotificationProvider {
    fun createForegroundInfo(workId: UUID, title: String, text: String, progress: Float): ForegroundInfo
    fun showPaused(workId: UUID, title: String, text: String, progress: Float, cancelIntent: PendingIntent)
    fun showResult(workId: UUID, title: String, text: String)
    fun cancel(workId: UUID)
}
