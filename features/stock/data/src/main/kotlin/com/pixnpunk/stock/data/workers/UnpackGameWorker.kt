package com.pixnpunk.stock.data.workers

import android.content.Context
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.notification.NotificationProvider
import com.pixnpunk.stock.data.workers.WorkerKeys.GAME_DATA_JSON
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.utils.DirUtil.receiveGameDirFromFile
import com.pixnpunk.utils.FileUtil.removeFileByPath
import com.pixnpunk.utils.FileUtil.unpackFileToDir
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent

class UnpackGameWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val logger: AppLogger,
    private val localRepository: LocalRepository,
    private val notificationProvider: NotificationProvider
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    override suspend fun doWork(): Result {
        val archivePath = inputData.getString("filePath") ?: return Result.failure()
        val destDirPath = inputData.getString("destDirPath") ?: return Result.failure()
        val dataJson = inputData.getString(GAME_DATA_JSON) ?: return Result.failure()
        val gameEntry = Json.decodeFromString<GameEntry>(dataJson)

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
        } catch (e: Exception) {
            logger.apply {
                recordBreadcrumb("Failed to unpack | file $archivePath")
                recordNonFatalException(e)
            }
            notificationProvider.showNotification(
                "Error",
                "Failed to unpack file"
            )
            destDirPath.toUri().toFile().deleteRecursively()
            Result.failure()
        }
    }
}
