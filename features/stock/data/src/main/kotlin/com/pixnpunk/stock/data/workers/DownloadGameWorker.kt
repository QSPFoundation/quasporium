package com.pixnpunk.stock.data.workers

import android.content.Context
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.notification.NotificationProvider
import com.pixnpunk.stock.data.R
import com.pixnpunk.stock.data.download.GameDownloader
import com.pixnpunk.stock.data.download.isTransientDownloadError
import com.pixnpunk.stock.data.mapper.toNetworkError
import com.pixnpunk.stock.data.workers.WorkerKeys.DEST_DIR
import com.pixnpunk.stock.data.workers.WorkerKeys.DOWNLOAD_URL
import com.pixnpunk.stock.domain.model.NetworkError
import io.ktor.client.HttpClient
import java.util.LinkedList
import kotlin.coroutines.cancellation.CancellationException

class DownloadGameWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val logger: AppLogger,
    private val notificationProvider: NotificationProvider,
    httpClient: HttpClient
) : CoroutineWorker(appContext, workerParams) {

    private val downloader = GameDownloader(httpClient)
    private var isForeground = true

    private companion object {
        const val MAX_ATTEMPTS = 5
        const val PROGRESS_INTERVAL_SECONDS = 0.5
        const val SPEED_SAMPLES = 5
    }

    private suspend fun updateForeground(title: String, progress: Float) {
        if (!isForeground) return
        isForeground = trySetForeground(
            notificationProvider.createForegroundInfo(id, title, progress)
        )
        if (!isForeground) {
            logger.recordBreadcrumb("Foreground service is not allowed, download continues in background")
        }
    }

    private fun failureMessage(error: Throwable): String =
        when (val networkError = error.toNetworkError()) {
            NetworkError.NoConnection -> applicationContext.getString(R.string.download_error_no_connection)
            NetworkError.Timeout -> applicationContext.getString(R.string.download_error_timeout)
            NetworkError.NotFound -> applicationContext.getString(R.string.download_error_not_found)
            is NetworkError.Server ->
                applicationContext.getString(R.string.download_error_server, networkError.code)
            NetworkError.Unexpected -> applicationContext.getString(R.string.download_error_unexpected)
        }

    private fun progressReporter(): suspend (Long, Long?) -> Unit {
        var lastTime = System.currentTimeMillis()
        var lastBytes = -1L
        val speedSamples = LinkedList<Double>()

        return { downloaded, total ->
            val currentTime = System.currentTimeMillis()
            val timeDelta = (currentTime - lastTime) / 1000.0

            if (lastBytes < 0) {
                lastBytes = downloaded
            } else if (total != null && timeDelta > PROGRESS_INTERVAL_SECONDS) {
                speedSamples.add((downloaded - lastBytes) / timeDelta)
                if (speedSamples.size > SPEED_SAMPLES) speedSamples.removeFirst()

                lastTime = currentTime
                lastBytes = downloaded

                val averageSpeed = speedSamples.average()
                val etaSeconds =
                    if (averageSpeed > 0) (total - downloaded) / averageSpeed
                    else Double.POSITIVE_INFINITY

                updateForeground(
                    "Downloading: $etaSeconds",
                    downloaded.toFloat() / total.toFloat()
                )
            }
        }
    }

    override suspend fun doWork(): Result {
        val downloadLink = inputData.getString(DOWNLOAD_URL) ?: return Result.failure()
        val destDirPath = inputData.getString(DEST_DIR) ?: return Result.failure()
        val destDir = destDirPath.toUri().toFile()

        updateForeground("Preparing to download", 0f)

        return try {
            val file = downloader.download(downloadLink, destDir, progressReporter())
            Result.success(
                workDataOf(
                    "filePath" to file.toUri().toString(),
                    "destDirPath" to destDirPath
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e.isTransientDownloadError() && runAttemptCount < MAX_ATTEMPTS - 1) {
                logger.recordBreadcrumb(
                    "Download interrupted, retrying | attempt: $runAttemptCount | " +
                        "link: $downloadLink | ${e.javaClass.simpleName}"
                )
                return Result.retry()
            }

            logger.apply {
                recordBreadcrumb("Failed to download file | link: $downloadLink")
                recordNonFatalException(e)
            }

            notificationProvider.showNotification(
                applicationContext.getString(R.string.download_error_title),
                failureMessage(e)
            )

            destDir.deleteRecursively()
            Result.failure()
        }
    }
}
