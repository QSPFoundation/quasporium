package com.pixnpunk.stock.data.workers

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pixnpunk.dto.stringToEntry
import com.pixnpunk.notification.NotificationProvider
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.utils.DirUtil.receiveGameDirFromFile
import com.pixnpunk.utils.FileUtil.removeFileByPath
import com.pixnpunk.utils.FileUtil.unpackFileToDir
import org.koin.core.component.KoinComponent

class UnpackGameWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val localRepository: LocalRepository,
    private val notificationProvider: NotificationProvider
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    override suspend fun doWork(): Result {
        val archivePath = inputData.getString("filePath") ?: return Result.failure()
        val destDirPath = inputData.getString("destDirPath") ?: return Result.failure()
        val dataJson = inputData.getString("gameDataJson") ?: return Result.failure()
        val gameEntry = dataJson.stringToEntry()

        setForeground(
            notificationProvider.createForegroundInfo(
                id,
                "Preparing to unpack",
                0f
            )
        )

        return try {
            unpackFileToDir(
                filePath = archivePath,
                destDirPath = destDirPath,
                onProgressUpdate = { state ->
                    setForeground(
                        notificationProvider.createForegroundInfo(
                            id,
                            "Unpacking: ${state.time}",
                            state.progress
                        )
                    )
                }
            ).getOrThrow()

            archivePath.removeFileByPath()

            val newGameDir = receiveGameDirFromFile(destDirPath)
            if (newGameDir == null || destDirPath.toUri() == newGameDir.uri) {
                localRepository.fillAndInsertEntry(
                    gameEntry.copy(gameDirPath = destDirPath)
                )
            } else {
                localRepository.fillAndInsertEntry(
                    gameEntry.copy(gameDirPath = newGameDir.uri.toString())
                )
            }

            notificationProvider.showNotification(
                "Installation complete",
                "${gameEntry.title} is ready to play!"
            )

            Result.success()
        } catch (_: Exception) {
            notificationProvider.showNotification(
                "Error",
                "Failed to unpack file"
            )
            Result.failure()
        }
    }
}