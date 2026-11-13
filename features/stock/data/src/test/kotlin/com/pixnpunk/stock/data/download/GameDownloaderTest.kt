package com.pixnpunk.stock.data.download

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class GameDownloaderTest {

    private companion object {
        const val DOWNLOAD_URL = "https://qsp.org/games/305/download"
        const val FILE_URL = "https://qsp.org/storage/games/305/goblins.zip"
        const val ETAG = "\"6553f1a2-a\""
        val GAME = "0123456789".toByteArray()
    }

    @get:Rule
    val folder = TemporaryFolder()

    private val requests = mutableListOf<HttpRequestData>()

    private val stateFile get() = File(folder.root, DownloadState.FILE_NAME)
    private val gameFile get() = File(folder.root, "goblins.zip")

    private fun downloader(
        handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ) =
        GameDownloader(
            HttpClient(
                MockEngine { request ->
                    requests += request
                    handler(request)
                }
            ) { install(HttpTimeout) }
        )

    private fun download(downloader: GameDownloader) =
        runBlocking {
            downloader.download(DOWNLOAD_URL, folder.root) { _, _ -> }
        }

    private fun MockRequestHandleScope.redirectThen(
        request: HttpRequestData,
        file: MockRequestHandleScope.() -> HttpResponseData
    ): HttpResponseData =
        if (request.url.toString() == DOWNLOAD_URL)
            respond(
                "",
                HttpStatusCode.Found,
                headersOf(HttpHeaders.Location, FILE_URL)
            )
        else file()

    private fun MockRequestHandleScope.fullFile(
        body: ByteArray = GAME,
        length: Int = body.size
    ) =
        respond(
            body,
            HttpStatusCode.OK,
            headersOf(
                HttpHeaders.ContentLength to listOf(length.toString()),
                HttpHeaders.ETag to listOf(ETAG)
            )
        )

    private fun savePartial(state: DownloadState, bytes: ByteArray = GAME.copyOf(4)) {
        gameFile.writeBytes(bytes)
        stateFile.writeText(Json.encodeToString(state))
    }

    private val savedState = DownloadState(
        url = FILE_URL,
        fileName = "goblins.zip",
        totalSize = GAME.size.toLong(),
        etag = ETAG
    )

    @Test
    fun downloadsTheFileBehindTheRedirect() {
        val file = download(downloader { redirectThen(it) { fullFile() } })

        assertEquals(gameFile, file)
        assertArrayEquals(GAME, file.readBytes())
        assertFalse(stateFile.exists())
    }

    @Test
    fun interruptedDownloadKeepsTheFileAndWhereItCameFrom() {
        val downloader = downloader {
            redirectThen(it) {
                fullFile(
                    GAME.copyOf(4),
                    GAME.size
                )
            }
        }

        val error = runCatching { download(downloader) }.exceptionOrNull()

        assertTrue(error is IOException)
        assertTrue(error!!.isTransientDownloadError())
        assertArrayEquals(GAME.copyOf(4), gameFile.readBytes())
        assertEquals(savedState, Json.decodeFromString<DownloadState>(stateFile.readText()))
    }

    @Test
    fun resumeRequestsTheRestFromTheFileUrl() {
        savePartial(savedState)

        val file = download(
            downloader {
                respond(
                    GAME.copyOfRange(4, GAME.size),
                    HttpStatusCode.PartialContent,
                    headersOf(HttpHeaders.ContentRange, "bytes 4-9/10")
                )
            }
        )

        val request = requests.single()
        assertEquals(FILE_URL, request.url.toString())
        assertEquals("bytes=4-", request.headers[HttpHeaders.Range])
        assertEquals(ETAG, request.headers[HttpHeaders.IfRange])
        assertArrayEquals(GAME, file.readBytes())
        assertFalse(stateFile.exists())
    }

    @Test
    fun changedFileIsDownloadedAgain() {
        savePartial(savedState)
        val newGame = "abcdefghijkl".toByteArray()

        val file = download(downloader { fullFile(newGame) })

        assertEquals(1, requests.size)
        assertArrayEquals(newGame, file.readBytes())
    }

    @Test
    fun completeFileIsNotDownloadedAgain() {
        savePartial(savedState, GAME)

        val file = download(downloader { respond("", HttpStatusCode.RequestedRangeNotSatisfiable) })

        assertEquals(1, requests.size)
        assertArrayEquals(GAME, file.readBytes())
    }

    @Test
    fun missingFileStartsOverFromTheDownloadLink() {
        savePartial(savedState)

        val file = download(
            downloader { request ->
                when (request.url.toString()) {
                    DOWNLOAD_URL -> respond(
                        "", HttpStatusCode.Found,
                        headersOf(
                            HttpHeaders.Location,
                            "https://qsp.org/storage/games/305/goblins-v2.zip"
                        )
                    )

                    FILE_URL -> respond("", HttpStatusCode.NotFound)
                    else -> fullFile()
                }
            }
        )

        assertEquals("goblins-v2.zip", file.name)
        assertArrayEquals(GAME, file.readBytes())
        assertFalse(gameFile.exists())
    }

    @Test
    fun weakEtagWithoutDateStartsOver() {
        savePartial(savedState.copy(etag = "W/\"6553f1a2\""))

        val file = download(downloader { redirectThen(it) { fullFile() } })

        assertNull(requests.first().headers[HttpHeaders.Range])
        assertEquals(DOWNLOAD_URL, requests.first().url.toString())
        assertArrayEquals(GAME, file.readBytes())
    }

    @Test
    fun fileNameCannotLeaveTheGameDir() {
        val file = download(
            downloader {
                respond(
                    GAME,
                    HttpStatusCode.OK,
                    headersOf(
                        HttpHeaders.ContentDisposition,
                        "attachment; filename=\"../../evil.qsp\""
                    )
                )
            }
        )

        assertEquals(File(folder.root, "evil.qsp"), file)
    }

    @Test
    fun onlyNetworkFailuresAreRetried() {
        val notFound = runCatching {
            download(downloader { redirectThen(it) { respond("", HttpStatusCode.NotFound) } })
        }.exceptionOrNull()!!
        val unavailable = runCatching {
            download(downloader { respond("", HttpStatusCode.ServiceUnavailable) })
        }.exceptionOrNull()!!

        assertFalse(notFound.isTransientDownloadError())
        assertTrue(unavailable.isTransientDownloadError())
        assertTrue(IOException("Connection reset").isTransientDownloadError())
        assertFalse(DownloadStorageException(IOException("ENOSPC")).isTransientDownloadError())
    }
}
