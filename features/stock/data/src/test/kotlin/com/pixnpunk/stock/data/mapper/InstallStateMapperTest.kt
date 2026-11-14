package com.pixnpunk.stock.data.mapper

import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.workDataOf
import com.pixnpunk.stock.data.workers.InstallStep
import com.pixnpunk.stock.data.workers.WorkerKeys.PROGRESS_STEP
import com.pixnpunk.stock.data.workers.WorkerKeys.PROGRESS_VALUE
import com.pixnpunk.stock.domain.model.InstallState
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class InstallStateMapperTest {

    private fun info(
        state: WorkInfo.State,
        runAttemptCount: Int = 0,
        progress: Data = Data.EMPTY
    ) = WorkInfo(UUID.randomUUID(), state, emptySet(), Data.EMPTY, progress, runAttemptCount)

    private fun running(step: InstallStep, progress: Float) =
        info(
            WorkInfo.State.RUNNING,
            progress = workDataOf(PROGRESS_STEP to step.name, PROGRESS_VALUE to progress)
        )

    @Test
    fun noWorkMeansNotStarted() {
        assertEquals(InstallState.NotStarted, (null as WorkInfo?).toInstallState())
        assertEquals(InstallState.NotStarted, info(WorkInfo.State.CANCELLED).toInstallState())
    }

    @Test
    fun enqueuedWorkIsQueuedUntilItHasRun() {
        assertEquals(InstallState.Queued, info(WorkInfo.State.ENQUEUED).toInstallState())
        assertEquals(InstallState.Paused, info(WorkInfo.State.ENQUEUED, runAttemptCount = 1).toInstallState())
    }

    @Test
    fun runningWorkReportsItsStep() {
        assertEquals(InstallState.Downloading(0.25f), running(InstallStep.DOWNLOAD, 0.25f).toInstallState())
        assertEquals(InstallState.Unpacking(0.5f), running(InstallStep.UNPACK, 0.5f).toInstallState())
        assertEquals(InstallState.Downloading(0f), info(WorkInfo.State.RUNNING).toInstallState())
    }

    @Test
    fun finishedWorkIsInstalledOrFailed() {
        assertEquals(InstallState.Installed, info(WorkInfo.State.SUCCEEDED).toInstallState())
        assertEquals(InstallState.Failed, info(WorkInfo.State.FAILED).toInstallState())
    }
}
