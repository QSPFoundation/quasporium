package com.pixnpunk.stock.data.workers

import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo

internal suspend fun CoroutineWorker.trySetForeground(foregroundInfo: ForegroundInfo): Boolean =
    try {
        setForeground(foregroundInfo)
        true
    } catch (_: IllegalStateException) {
        false
    }
