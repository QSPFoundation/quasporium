package com.pixnpunk.stock.data.workers

import android.content.Context
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.notification.NotificationProvider
import com.pixnpunk.stock.data.workers.WorkerKeys.DEST_DIR
import com.pixnpunk.stock.data.workers.WorkerKeys.DOWNLOAD_URL
import com.pixnpunk.storage.FileUtil.writeFileToDir
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.minutes

class DownloadGameWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val logger: AppLogger,
    private val notificationProvider: NotificationProvider,
    private val httpClient: HttpClient
) : CoroutineWorker(appContext, workerParams) {

    private var isForeground = true

    private suspend fun updateForeground(title: String, progress: Float) {
        if (!isForeground) return
        isForeground = trySetForeground(
            notificationProvider.createForegroundInfo(id, title, progress)
        )
        if (!isForeground) {
            logger.recordBreadcrumb("Foreground service is not allowed, download continues in background")
        }
    }

    fun extractFileName(response: HttpResponse): String? {
        val contentDisposition = response.headers[HttpHeaders.ContentDisposition]
        if (!contentDisposition.isNullOrBlank()) {
            val parsed = parseHeaderValue(contentDisposition).firstOrNull()

            val filenameStar =
                parsed?.params?.find { it.name.equals("filename*", ignoreCase = true) }?.value
            if (!filenameStar.isNullOrBlank()) {
                val encodedValue = filenameStar.substringAfter("''", filenameStar)
                runCatching {
                    return URLDecoder.decode(encodedValue, StandardCharsets.UTF_8.name()).trim('"')
                }
            }

            val filename =
                parsed?.params?.find { it.name.equals("filename", ignoreCase = true) }?.value
            if (!filename.isNullOrBlank()) {
                return filename.trim('"')
            }
        }

        val urlPathName = response.request.url.segments.lastOrNull { it.isNotBlank() }
        if (!urlPathName.isNullOrBlank()) {
            val decoded = runCatching {
                URLDecoder.decode(urlPathName, StandardCharsets.UTF_8.name())
            }.getOrDefault(urlPathName)

            if (decoded.isNotBlank()) {
                return decoded
            }
        }

        return null
    }

    override suspend fun doWork(): Result {
        val downloadLink = inputData.getString(DOWNLOAD_URL) ?: return Result.failure()
        val destDirPath = inputData.getString(DEST_DIR) ?: return Result.failure()

        updateForeground("Preparing to download", 0f)

        return try {
            httpClient.prepareGet(
                urlString = downloadLink,
                block = {
                    header(HttpHeaders.CacheControl, "no-cache, no-store")

                    var lastTime = System.currentTimeMillis()
                    var lastBytesSent = 0L
                    val speedSamples = LinkedList<Double>()

                    val timeout = 30.minutes.inWholeMilliseconds
                    timeout {
                        requestTimeoutMillis = timeout
                        connectTimeoutMillis = timeout
                        socketTimeoutMillis = timeout
                    }

                    onDownload { bytesSentTotal, contentLength ->
                        if (contentLength != null) {
                            val currentTime = System.currentTimeMillis()
                            val timeDelta = (currentTime - lastTime) / 1000.0

                            if (timeDelta > 0.5) {
                                val instantSpeed = (bytesSentTotal - lastBytesSent) / timeDelta

                                speedSamples.add(instantSpeed)
                                if (speedSamples.size > 5) {
                                    speedSamples.removeFirst()
                                }

                                lastTime = currentTime
                                lastBytesSent = bytesSentTotal

                                val averageSpeed = speedSamples.average()
                                val etaSeconds =
                                    if (averageSpeed > 0) {
                                        (contentLength - bytesSentTotal) / averageSpeed
                                    } else {
                                        Double.POSITIVE_INFINITY
                                    }

                                updateForeground(
                                    "Downloading: $etaSeconds",
                                    bytesSentTotal.toFloat() / contentLength.toFloat()
                                )
                            }
                        }
                    }
                }
            ).execute { response ->
                if (!response.status.isSuccess()) {
                    throw RuntimeException("HTTP Error ${response.status}")
                }

                val file = writeFileToDir(
                    fileChannel = response.bodyAsChannel(),
                    fileName = extractFileName(response)
                        ?: throw RuntimeException("Could not resolve file name"),
                    destDirPath = destDirPath,
                    totalSize = response.contentLength() ?: -1L,
                    onProgressUpdate = { state ->
                        updateForeground("Writing: ${state.time}", state.progress)
                    }
                )

                Result.success(
                    workDataOf(
                        "filePath" to file.getOrThrow(),
                        "destDirPath" to destDirPath
                    )
                )
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                destDirPath.toUri().toFile().deleteRecursively()
                throw e
            }

            logger.apply {
                recordBreadcrumb("Failed to download file | link: $downloadLink")
                recordNonFatalException(e)
            }

            notificationProvider.showNotification(
                "Error",
                "Failed to download file"
            )

            destDirPath.toUri().toFile().deleteRecursively()
            Result.failure()
        }
    }
}
