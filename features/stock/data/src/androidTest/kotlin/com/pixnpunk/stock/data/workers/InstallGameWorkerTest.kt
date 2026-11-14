package com.pixnpunk.stock.data.workers

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.net.toUri
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.ForegroundUpdater
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.notification.NotificationProvider
import com.pixnpunk.stock.data.download.GameDownloader
import com.pixnpunk.stock.data.workers.WorkerKeys.DOWNLOAD_URL
import com.pixnpunk.stock.data.workers.WorkerKeys.GAME_DATA_JSON
import com.pixnpunk.stock.data.workers.WorkerKeys.GAME_DIR_NAME
import com.pixnpunk.stock.domain.model.SearchState
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.storage.ArchiveExtractor
import com.pixnpunk.storage.di.storageModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.CopyOnWriteArrayList
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class InstallGameWorkerTest {

    private companion object {
        const val GAME_DIR = "workers-test/Game"
        const val DOWNLOAD_LINK = "https://example.org/download?id=1"
    }

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val logger = RecordingLogger()
    private val notifications = RecordingNotifications()
    private val repository = RecordingLocalRepository()
    private val gameDir = File(context.filesDir, GAME_DIR)
    private val requests = CopyOnWriteArrayList<String>()

    private var server: MockRequestHandler = { error("No request expected") }

    private val deniedForeground = ForegroundUpdater { _, _, _ ->
        CallbackToFutureAdapter.getFuture {
            it.setException(IllegalStateException("Foreground service start not allowed"))
        }
    }

    private val allowedForeground = ForegroundUpdater { _, _, _ ->
        CallbackToFutureAdapter.getFuture { it.set(null) }
    }

    private val input: Data = workDataOf(
        DOWNLOAD_URL to DOWNLOAD_LINK,
        GAME_DIR_NAME to GAME_DIR,
        GAME_DATA_JSON to Json.encodeToString(GameEntry(title = "Test"))
    )

    private val workerFactory = object : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ) = InstallGameWorker(
            appContext, workerParameters, logger, repository, notifications,
            GlobalContext.get().get<ArchiveExtractor>(),
            GameDownloader(
                HttpClient(
                    MockEngine { request ->
                        requests += request.url.toString()
                        server(request)
                    }
                ) { install(HttpTimeout) }
            )
        )
    }

    private fun worker(
        foregroundUpdater: ForegroundUpdater = deniedForeground,
        id: UUID = UUID.randomUUID()
    ) =
        TestListenableWorkerBuilder<InstallGameWorker>(context, input)
            .setId(id)
            .setForegroundUpdater(foregroundUpdater)
            .setWorkerFactory(workerFactory)
            .build()

    private fun zip(entries: Map<String, ByteArray>): ByteArray =
        ByteArrayOutputStream().also { bytes ->
            ZipOutputStream(bytes).use { zip ->
                zip.setLevel(Deflater.NO_COMPRESSION)
                for ((path, content) in entries) {
                    zip.putNextEntry(ZipEntry(path))
                    zip.write(content)
                    zip.closeEntry()
                }
            }
        }.toByteArray()

    private fun serve(fileName: String, content: ByteArray) {
        server = {
            respond(
                content = content,
                headers = headersOf(
                    HttpHeaders.ContentDisposition to listOf("attachment; filename=\"$fileName\""),
                    HttpHeaders.ContentLength to listOf(content.size.toString())
                )
            )
        }
    }

    private fun bigArchive(): ByteArray {
        val payload = Random(0).nextBytes(256 * 1024)
        return zip((0 until 200).associate { "file$it.bin" to payload })
    }

    private suspend fun awaitText(prefix: String) {
        val started = CompletableDeferred<Unit>()
        notifications.onText = { if (it.startsWith(prefix)) started.complete(Unit) }
        withTimeout(10.seconds) { started.await() }
    }

    @Before
    fun setUp() {
        startKoin {
            androidContext(context)
            modules(storageModule)
        }
        gameDir.parentFile?.deleteRecursively()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setWorkerFactory(workerFactory).build()
        )
    }

    @After
    fun tearDown() {
        stopKoin()
        gameDir.parentFile?.deleteRecursively()
    }

    @Test
    fun installsAnArchiveWithoutForegroundPermission() = runBlocking {
        val game = "game".toByteArray()
        serve("game.zip", zip(mapOf("game.qsp" to game)))

        val result = worker().doWork()

        assertEquals(emptyList<Throwable>(), logger.errors)
        assertEquals(ListenableWorker.Result.success(), result)
        assertArrayEquals(game, File(gameDir, "game.qsp").readBytes())
        assertEquals(listOf("game.qsp"), gameDir.list()?.toList())
        assertEquals(listOf(gameDir.toUri().toString()), repository.inserted.map { it.gameDirPath })
        assertEquals(listOf("Test is ready to play: Installation complete."), notifications.shown)
        assertTrue(logger.breadcrumbs.any { "Foreground service is not allowed" in it })
    }

    @Test
    fun installsAGameFileWithoutUnpacking() = runBlocking {
        val game = "game".toByteArray()
        serve("game.qsp", game)

        val result = worker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(listOf("game.qsp"), gameDir.list()?.toList())
        assertEquals(listOf(gameDir.toUri().toString()), repository.inserted.map { it.gameDirPath })
    }

    @Test
    fun retryAfterDownloadUnpacksWithoutDownloadingAgain() = runBlocking {
        gameDir.mkdirs()
        File(gameDir, "game.zip").writeBytes(zip(mapOf("game.qsp" to "game".toByteArray())))
        File(gameDir, ".archive").writeText("game.zip")

        val result = worker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(emptyList<String>(), requests)
        assertEquals(listOf("game.qsp"), gameDir.list()?.toList())
    }

    @Test
    fun interruptedDownloadIsRetriedAndKeepsTheFile() = runBlocking {
        server = {
            respond(
                content = "ga".toByteArray(),
                headers = headersOf(
                    HttpHeaders.ContentDisposition to listOf("attachment; filename=\"game.qsp\""),
                    HttpHeaders.ContentLength to listOf("4"),
                    HttpHeaders.ETag to listOf("\"1\"")
                )
            )
        }

        val result = worker().doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
        assertEquals(emptyList<Throwable>(), logger.errors)
        assertEquals(emptyList<String>(), notifications.shown)
        assertEquals(listOf("Test: Paused · waiting for network"), notifications.paused)
        assertArrayEquals("ga".toByteArray(), File(gameDir, "game.qsp").readBytes())
        assertTrue(File(gameDir, ".download").exists())
    }

    @Test
    fun missingDownloadFailsAndDeletesGameDir() = runBlocking {
        server = { respond(content = "", status = HttpStatusCode.NotFound) }

        val result = worker().doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
        assertEquals(listOf("Couldn't install Test: The game file was not found."), notifications.shown)
        assertFalse(gameDir.exists())
    }

    @Test
    fun brokenArchiveFailsAndDeletesGameDir() = runBlocking {
        serve("game.zip", "not a zip".toByteArray())

        val result = worker().doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
        assertEquals(listOf("Couldn't install Test: Could not unpack the game archive."), notifications.shown)
        assertFalse(gameDir.exists())
    }

    @Test
    fun stopBySystemKeepsTheDownloadedArchive() = runBlocking {
        serve("big.zip", bigArchive())
        val future = worker(allowedForeground).startWork()

        awaitText("Unpacking")
        future.cancel(true)
        delay(500.milliseconds)

        assertTrue(File(gameDir, "big.zip").exists())
        assertTrue(File(gameDir, ".archive").exists())
        assertTrue(repository.inserted.isEmpty())
    }

    @Test
    fun cancelByUserDeletesGameDir() = runBlocking {
        serve("big.zip", bigArchive())
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<InstallGameWorker>()
            .setInputData(input)
            .setInitialDelay(1, TimeUnit.DAYS)
            .build()
        workManager.enqueue(request).result.get()
        val future = worker(allowedForeground, request.id).startWork()

        awaitText("Unpacking")
        workManager.cancelWorkById(request.id).result.get()
        future.cancel(true)

        withTimeout(10.seconds) {
            while (gameDir.exists()) delay(50.milliseconds)
        }
        assertTrue(repository.inserted.isEmpty())
    }

    private class RecordingLogger : AppLogger {
        val breadcrumbs = CopyOnWriteArrayList<String>()
        val errors = CopyOnWriteArrayList<Throwable>()

        override fun recordBreadcrumb(message: String) {
            breadcrumbs += message
        }

        override fun recordNonFatalException(throwable: Throwable) {
            errors += throwable
        }
    }

    private inner class RecordingNotifications : NotificationProvider {
        @Volatile
        var onText: (String) -> Unit = {}
        val titles = CopyOnWriteArrayList<String>()
        val paused = CopyOnWriteArrayList<String>()
        val shown = CopyOnWriteArrayList<String>()

        override fun createForegroundInfo(
            workId: UUID,
            title: String,
            text: String,
            progress: Float
        ): ForegroundInfo {
            titles += title
            onText(text)
            val notification = Notification.Builder(context, "test")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .build()
            return ForegroundInfo(1, notification)
        }

        override fun showPaused(
            workId: UUID,
            title: String,
            text: String,
            progress: Float,
            cancelIntent: PendingIntent
        ) {
            paused += "$title: $text"
        }

        override fun showResult(workId: UUID, title: String, text: String) {
            shown += "$title: $text"
        }

        override fun cancel(workId: UUID) = Unit
    }

    private class RecordingLocalRepository : LocalRepository {
        val inserted = CopyOnWriteArrayList<GameEntry>()

        override suspend fun fillAndInsertEntry(entry: GameEntry) {
            inserted += entry
        }

        override fun observeEntryUrls(): Flow<List<String>> = unused()
        override fun observeEntryById(entryId: Long): Flow<GameEntry?> = unused()
        override fun getGameEntriesPager(searchState: SearchState): Flow<PagingData<GameEntry>> =
            unused()
        override suspend fun deleteGameEntries(idsDelete: Set<Long>): Int = unused()
        override suspend fun getGameEntryById(entryId: Long): GameEntry = unused()
        override suspend fun updateEntry(entry: GameEntry) = unused()
        override suspend fun upsertEntry(entry: GameEntry) = unused()
        override suspend fun insertEntry(newEntry: GameEntry) = unused()
        override suspend fun migrateIfNeeded() = unused()

        private fun unused(): Nothing = error("Not used by the worker")
    }
}
