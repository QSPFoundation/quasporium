package com.pixnpunk.stock.data.workers

import android.content.Context
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.pixnpunk.notification.NotificationProvider
import com.pixnpunk.utils.FileUtil.writeFileToDir
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.*
import kotlin.time.Duration.Companion.minutes

class DownloadGameWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val notificationProvider: NotificationProvider
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    override suspend fun doWork(): Result {
        val downloadLink = inputData.getString("url") ?: return Result.failure()
        val destDirPath = inputData.getString("destDir") ?: return Result.failure()

        setForeground(
            notificationProvider.createForegroundInfo(
                id,
                "Preparing to download",
                0f
            )
        )

        return try {
            get<HttpClient>().prepareGet(
                urlString = downloadLink,
                block = {
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
                                    if (averageSpeed > 0) (contentLength - bytesSentTotal) / averageSpeed
                                    else Double.POSITIVE_INFINITY

                                setForeground(
                                    notificationProvider.createForegroundInfo(
                                        id,
                                        "Downloading: $etaSeconds",
                                        bytesSentTotal.toFloat() / contentLength.toFloat()
                                    )
                                )
                            }
                        }
                    }
                }
            ).execute { response ->
                if (!response.status.isSuccess()) {
                    throw RuntimeException("HTTP Error ${response.status}")
                }

                val contentDisposition = response.headers[HttpHeaders.ContentDisposition]
                val headerValues = parseHeaderValue(contentDisposition)
                val fileName = headerValues.firstOrNull()?.params?.firstOrNull()?.value
                    ?: "#${(0..Int.MAX_VALUE).random()}Game"

                val file = writeFileToDir(
                    fileChannel = response.bodyAsChannel(),
                    fileName = fileName,
                    destDirPath = destDirPath,
                    totalSize = response.contentLength() ?: -1L,
                    onProgressUpdate = { state ->
                        setForeground(
                            notificationProvider.createForegroundInfo(
                                id,
                                "Writing: ${state.time}",
                                state.progress
                            )
                        )
                    }
                )

                Result.success(
                    workDataOf(
                        "filePath" to file.getOrThrow(),
                        "destDirPath" to destDirPath
                    )
                )
            }
        } catch (_: Exception) {
            notificationProvider.showNotification(
                "Error",
                "Failed to download file"
            )
            destDirPath.toUri().toFile().deleteRecursively()
            Result.failure()
        }
    }
}