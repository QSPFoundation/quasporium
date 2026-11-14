package com.pixnpunk.stock.data.mapper

import androidx.work.WorkInfo
import com.pixnpunk.stock.data.workers.InstallStep
import com.pixnpunk.stock.data.workers.WorkerKeys.PROGRESS_STEP
import com.pixnpunk.stock.data.workers.WorkerKeys.PROGRESS_VALUE
import com.pixnpunk.stock.domain.model.InstallState

internal fun WorkInfo?.toInstallState(): InstallState =
    when (this?.state) {
        null, WorkInfo.State.CANCELLED, WorkInfo.State.SUCCEEDED -> InstallState.NotStarted

        WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED ->
            if (runAttemptCount > 0) InstallState.Paused else InstallState.Queued

        WorkInfo.State.RUNNING -> {
            val value = progress.getFloat(PROGRESS_VALUE, 0f)
            if (progress.getString(PROGRESS_STEP) == InstallStep.UNPACK.name) InstallState.Unpacking(value)
            else InstallState.Downloading(value)
        }

        WorkInfo.State.FAILED -> InstallState.Failed
    }
