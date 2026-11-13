package com.pixnpunk.stock.data.workers

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.pixnpunk.common.TimeUtil.formatEta
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.notification.NotificationProvider
import com.pixnpunk.stock.data.R
import com.pixnpunk.stock.data.download.GameDownloader
import com.pixnpunk.stock.data.download.isTransientDownloadError
import com.pixnpunk.stock.data.mapper.toNetworkError
import com.pixnpunk.stock.data.workers.WorkerKeys.DOWNLOAD_URL
import com.pixnpunk.stock.data.workers.WorkerKeys.GAME_DATA_JSON
import com.pixnpunk.stock.data.workers.WorkerKeys.GAME_DIR_NAME
import com.pixnpunk.stock.data.workers.WorkerKeys.PROGRESS_STEP
import com.pixnpunk.stock.data.workers.WorkerKeys.PROGRESS_VALUE
import com.pixnpunk.stock.domain.model.NetworkError
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.storage.ArchiveExtractor
import com.pixnpunk.storage.DirUtil.receiveGameDirFromFile
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

internal class InstallGameWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val logger: AppLogger,
    private val localRepository: LocalRepository,
    private val notificationProvider: NotificationProvider,
    private val archiveExtractor: ArchiveExtractor,
    private val downloader: GameDownloader
) : CoroutineWorker(appContext, workerParams) {

    private var isForeground = true

    private companion object {
        const val MAX_ATTEMPTS = 5
        const val ARCHIVE_MARKER = ".archive"
        const val UNKNOWN_ETA = "--:--"
        const val PROGRESS_INTERVAL_MS = 500L
        const val SPEED_SAMPLES = 5
        val gameFileExtensions = setOf("qsp", "gam")
    }

    private suspend fun report(step: InstallStep, progress: Float, title: String) {
        setProgress(workDataOf(PROGRESS_STEP to step.name, PROGRESS_VALUE to progress))
        if (!isForeground) return
        isForeground = trySetForeground(
            notificationProvider.createForegroundInfo(id, title, progress)
        )
        if (!isForeground) {
            logger.recordBreadcrumb(
                "Foreground service is not allowed, installation continues in background"
            )
        }
    }

    private fun downloadProgress(): suspend (Long, Long?) -> Unit {
        var lastTime = 0L
        var lastBytes = 0L
        val speedSamples = ArrayDeque<Double>()

        return { downloaded, total ->
            val now = System.currentTimeMillis()
            if (lastTime == 0L) {
                lastTime = now
                lastBytes = downloaded
            } else if (now - lastTime >= PROGRESS_INTERVAL_MS) {
                speedSamples.addLast((downloaded - lastBytes).toDouble() / (now - lastTime))
                if (speedSamples.size > SPEED_SAMPLES) speedSamples.removeFirst()
                lastTime = now
                lastBytes = downloaded

                val bytesPerMs = speedSamples.average()
                val title =
                    if (total != null && bytesPerMs > 0)
                        applicationContext.getString(
                            R.string.install_downloading_eta,
                            ((total - downloaded) / bytesPerMs).toLong().formatEta()
                        )
                    else applicationContext.getString(R.string.install_downloading)
                val progress = if (total != null && total > 0) downloaded.toFloat() / total else 0f

                report(InstallStep.DOWNLOAD, progress, title)
            }
        }
    }

    private suspend fun download(url: String, gameDir: File): File {
        val marker = File(gameDir, ARCHIVE_MARKER)
        val downloaded =
            marker.takeIf { it.exists() }
                ?.let { File(gameDir, it.readText()) }
                ?.takeIf { it.exists() }
        if (downloaded != null) return downloaded

        report(InstallStep.DOWNLOAD, 0f, applicationContext.getString(R.string.install_preparing))
        val archive = downloader.download(url, gameDir, downloadProgress())
        marker.writeText(archive.name)
        return archive
    }

    private suspend fun unpack(archive: File, gameDir: File): String {
        val gameDirPath = gameDir.toUri().toString()
        if (archive.extension.lowercase(Locale.ROOT) in gameFileExtensions) return gameDirPath

        report(InstallStep.UNPACK, 0f, applicationContext.getString(R.string.install_unpacking))
        archiveExtractor.unpack(archive.toUri().toString(), gameDirPath) { state ->
            val title =
                if (state.time == UNKNOWN_ETA) applicationContext.getString(R.string.install_unpacking)
                else applicationContext.getString(R.string.install_unpacking_eta, state.time)
            report(InstallStep.UNPACK, state.progress, title)
        }.onFailure { if (it !is CancellationException) throw UnpackException(it) }.getOrThrow()

        archive.delete()
        return receiveGameDirFromFile(gameDirPath)?.uri?.toString() ?: gameDirPath
    }

    private suspend fun isCancelledByUser(): Boolean =
        withContext(NonCancellable) {
            runCatching {
                WorkManager.getInstance(applicationContext)
                    .getWorkInfoByIdFlow(id)
                    .first()
                    ?.state == WorkInfo.State.CANCELLED
            }.getOrDefault(false)
        }

    private fun failureMessage(error: Throwable): String =
        if (error is UnpackException) applicationContext.getString(R.string.install_error_unpack)
        else when (val networkError = error.toNetworkError()) {
            NetworkError.NoConnection ->
                applicationContext.getString(R.string.download_error_no_connection)

            NetworkError.Timeout ->
                applicationContext.getString(R.string.download_error_timeout)

            NetworkError.NotFound ->
                applicationContext.getString(R.string.download_error_not_found)

            is NetworkError.Server ->
                applicationContext.getString(R.string.download_error_server, networkError.code)

            NetworkError.Unexpected ->
                applicationContext.getString(R.string.download_error_unexpected)
        }

    private fun readInput(): Triple<String, String, GameEntry>? {
        val url = inputData.getString(DOWNLOAD_URL)
        val gameDirName = inputData.getString(GAME_DIR_NAME)
        val entry =
            inputData.getString(GAME_DATA_JSON)?.let { Json.decodeFromString<GameEntry>(it) }
        return if (
            url != null
            && gameDirName != null
            && entry != null
        ) Triple(url, gameDirName, entry) else null
    }

    override suspend fun doWork(): Result {
        val (url, gameDirName, entry) = readInput() ?: return Result.failure()
        val gameDir = File(applicationContext.filesDir, gameDirName).apply { mkdirs() }

        return try {
            val archive = download(url, gameDir)
            val gameDirPath = unpack(archive, gameDir)
            File(gameDir, ARCHIVE_MARKER).delete()
            localRepository.fillAndInsertEntry(entry.copy(gameDirPath = gameDirPath))

            notificationProvider.showNotification(
                applicationContext.getString(
                    R.string.install_complete_title
                ),
                applicationContext.getString(
                    R.string.install_complete_text,
                    entry.title
                )
            )
            Result.success()
        } catch (e: CancellationException) {
            if (isCancelledByUser()) gameDir.deleteRecursively()
            throw e
        } catch (e: Exception) {
            if (e.isTransientDownloadError() && runAttemptCount < MAX_ATTEMPTS - 1) {
                logger.recordBreadcrumb(
                    "Installation interrupted, retrying | attempt: $runAttemptCount | " +
                            "link: $url | ${e.javaClass.simpleName}"
                )
                Result.retry()
            } else {
                logger.apply {
                    recordBreadcrumb("Failed to install game | link: $url")
                    recordNonFatalException(e)
                }

                notificationProvider.showNotification(
                    applicationContext.getString(R.string.install_error_title),
                    failureMessage(e)
                )

                gameDir.deleteRecursively()
                Result.failure()
            }
        }
    }
}
