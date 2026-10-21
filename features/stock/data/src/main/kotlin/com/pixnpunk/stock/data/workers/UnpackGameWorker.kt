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
import com.pixnpunk.storage.ArchiveExtractor
import com.pixnpunk.storage.DirUtil.receiveGameDirFromFile
import com.pixnpunk.storage.FileUtil.removeFileByPath
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

class UnpackGameWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val logger: AppLogger,
    private val localRepository: LocalRepository,
    private val notificationProvider: NotificationProvider,
    private val archiveExtractor: ArchiveExtractor
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private var isForeground = true

    private suspend fun updateForeground(title: String, progress: Float) {
        if (!isForeground) return
        isForeground = trySetForeground(
            notificationProvider.createForegroundInfo(id, title, progress)
        )
        if (!isForeground) {
            logger.recordBreadcrumb("Foreground service is not allowed, unpack continues in background")
        }
    }

    override suspend fun doWork(): Result {
        val archivePath = inputData.getString("filePath") ?: return Result.failure()
        val destDirPath = inputData.getString("destDirPath") ?: return Result.failure()
        val dataJson = inputData.getString(GAME_DATA_JSON) ?: return Result.failure()
        val gameEntry = Json.decodeFromString<GameEntry>(dataJson)

        val sourceFile = File(archivePath)
        val isDirectQspFile =
            sourceFile.extension.equals("qsp", ignoreCase = true) ||
            sourceFile.extension.equals("gam", ignoreCase = true)

        updateForeground(if (isDirectQspFile) "Installing game..." else "Preparing to unpack", 0f)

        return try {
            if (isDirectQspFile) {
                localRepository.fillAndInsertEntry(
                    gameEntry.copy(gameDirPath = destDirPath)
                )
            } else {
                archiveExtractor.unpack(
                    filePath = archivePath,
                    destDirPath = destDirPath,
                    onProgressUpdate = { state ->
                        updateForeground("Unpacking: ${state.time}", state.progress)
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
            }

            Result.success()
        } catch (e: Exception) {
            if (e is CancellationException) {
                destDirPath.toUri().toFile().deleteRecursively()
                throw e
            }

            logger.apply {
                recordBreadcrumb("Failed to unpack | extension: ${sourceFile.extension}")
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
