package com.pixnpunk.stock.data.download

import com.pixnpunk.stock.data.mapper.toNetworkError
import com.pixnpunk.stock.domain.model.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.plugins.retry
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.request
import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentLength
import io.ktor.http.etag
import io.ktor.http.parseHeaderValue
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.time.Duration.Companion.seconds

private const val FIRST_SERVER_ERROR = 500

private val contentRangeRegex = Regex("""bytes (\d+)-\d+/(\d+|\*)""")

internal class DownloadStorageException(cause: IOException) : Exception(cause.message, cause)

private inline fun <T> storage(block: () -> T): T =
    try {
        block()
    } catch (e: IOException) {
        throw DownloadStorageException(e)
    }

private fun safeFileName(name: String): String? =
    File(name.replace('\\', '/')).name
        .trim()
        .takeUnless { it.isEmpty() || it == "." || it == ".." || it == DownloadState.FILE_NAME }

private fun HttpResponse.fileName(): String? {
    val disposition =
        headers[HttpHeaders.ContentDisposition]?.let {
            parseHeaderValue(it).firstOrNull()
        }
    val encoded =
        disposition?.params
            ?.find {
                it.name.equals(
                    other = ContentDisposition.Parameters.FileNameAsterisk,
                    ignoreCase = true
                )
            }
            ?.value
            ?.let { value ->
                runCatching {
                    URLDecoder.decode(
                        value.substringAfter(
                            "''",
                            value
                        ),
                        StandardCharsets.UTF_8.name()
                    )
                }.getOrNull()
            }

    return listOfNotNull(
        encoded,
        disposition?.params?.find {
            it.name.equals(
                other = ContentDisposition.Parameters.FileName,
                ignoreCase = true
            )
        }?.value,
        request.url.segments.lastOrNull { it.isNotBlank() }
    ).firstNotNullOfOrNull { safeFileName(it.trim('"')) }
}

private fun HttpResponse.toState(
    fileName: String
) =
    DownloadState(
        url = request.url.toString(),
        fileName = fileName,
        totalSize = contentLength(),
        etag = etag(),
        lastModified = headers[HttpHeaders.LastModified]
    )

private fun HttpResponse.contentRange(): List<String>? =
    headers[HttpHeaders.ContentRange]?.let {
        contentRangeRegex.matchEntire(it)
    }?.groupValues

internal fun Throwable.isTransientDownloadError(): Boolean =
    when (val error = toNetworkError()) {
        NetworkError.NoConnection, NetworkError.Timeout -> true

        is NetworkError.Server ->
            error.code >= FIRST_SERVER_ERROR
                    || error.code == HttpStatusCode.TooManyRequests.value

        NetworkError.NotFound, NetworkError.Unexpected -> false
    }

internal class GameDownloader(
    private val httpClient: HttpClient
) {

    private companion object {
        const val BUFFER_SIZE = 8 * 1024
        val CONNECT_TIMEOUT = 15.seconds
        val SOCKET_TIMEOUT = 60.seconds
    }

    private fun readState(stateFile: File): DownloadState? =
        runCatching { Json.decodeFromString<DownloadState>(stateFile.readText()) }.getOrNull()

    private fun writeState(stateFile: File, state: DownloadState) =
        storage { stateFile.writeText(Json.encodeToString(state)) }

    private suspend fun <T> get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        block: suspend (HttpResponse) -> T
    ): T = httpClient.prepareGet(url) {
        expectSuccess = false
        retry { noRetry() }
        timeout {
            connectTimeoutMillis = CONNECT_TIMEOUT.inWholeMilliseconds
            socketTimeoutMillis = SOCKET_TIMEOUT.inWholeMilliseconds
            requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
        }
        header(HttpHeaders.CacheControl, "no-cache, no-store")
        headers.forEach { (name, value) -> header(name, value) }
    }.execute(block)

    private suspend fun copy(
        channel: ByteReadChannel,
        file: File,
        append: Boolean,
        totalSize: Long?,
        onProgress: suspend (downloaded: Long, total: Long?) -> Unit
    ) {
        var downloaded = if (append) file.length() else 0L

        storage { FileOutputStream(file, append) }.use { output ->
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                val read = channel.readAvailable(buffer)
                if (read == -1) break
                storage { output.write(buffer, 0, read) }
                downloaded += read
                onProgress(downloaded, totalSize)
            }
        }

        if (totalSize != null && downloaded != totalSize) {
            throw IOException("Downloaded $downloaded of $totalSize bytes")
        }
    }

    private suspend fun start(
        url: String,
        destDir: File,
        stateFile: File,
        onProgress: suspend (Long, Long?) -> Unit
    ): File = get(url) { response ->
        if (response.status != HttpStatusCode.OK) throw ResponseException(response, "")

        val fileName = response.fileName() ?: error("Could not resolve file name")
        val state = response.toState(fileName)
        writeState(stateFile, state)

        File(destDir, fileName).also {
            copy(response.bodyAsChannel(), it, append = false, state.totalSize, onProgress)
        }
    }

    private suspend fun resume(
        state: DownloadState,
        validator: String,
        partial: File,
        stateFile: File,
        onProgress: suspend (Long, Long?) -> Unit
    ): File? {
        val offset = partial.length()

        return get(
            url = state.url,
            headers = mapOf(
                HttpHeaders.Range to "bytes=$offset-",
                HttpHeaders.IfRange to validator
            )
        ) { response ->
            when (response.status) {
                HttpStatusCode.PartialContent -> {
                    val range = response.contentRange()
                    if (range?.get(1)?.toLongOrNull() != offset) return@get null
                    copy(
                        response.bodyAsChannel(), partial, append = true,
                        state.totalSize ?: range[2].toLongOrNull(), onProgress
                    )
                    partial
                }

                HttpStatusCode.OK -> {
                    val newState = response.toState(state.fileName).copy(url = state.url)
                    writeState(stateFile, newState)
                    copy(
                        response.bodyAsChannel(),
                        partial,
                        append = false,
                        newState.totalSize,
                        onProgress
                    )
                    partial
                }

                HttpStatusCode.RequestedRangeNotSatisfiable ->
                    partial.takeIf { offset == state.totalSize }

                HttpStatusCode.NotFound, HttpStatusCode.Gone -> null

                else -> throw ResponseException(response, "")
            }
        }
    }

    suspend fun download(
        url: String,
        destDir: File,
        onProgress: suspend (downloaded: Long, total: Long?) -> Unit
    ): File {
        val stateFile = File(destDir, DownloadState.FILE_NAME)
        val state = readState(stateFile)
        val partial = state?.let { File(destDir, it.fileName) }

        val resumed = state?.validator?.let { validator ->
            partial?.takeIf { it.length() > 0 }
                ?.let { resume(state, validator, it, stateFile, onProgress) }
        }

        if (resumed == null) partial?.delete()
        val file = resumed ?: start(url, destDir, stateFile, onProgress)
        stateFile.delete()
        return file
    }
}
