package com.pixnpunk.notification

import androidx.work.ForegroundInfo
import java.util.*

interface NotificationProvider {
    fun createForegroundInfo(workId: UUID, title: String, text: String, progress: Float): ForegroundInfo
    fun showPaused(workId: UUID, title: String, text: String, progress: Float)
    fun showResult(workId: UUID, title: String, text: String)
    fun cancel(workId: UUID)
}
