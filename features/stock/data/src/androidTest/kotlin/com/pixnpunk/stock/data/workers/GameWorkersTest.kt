package com.pixnpunk.stock.data.workers

import android.app.Notification
import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.net.toUri
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.ForegroundUpdater
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.notification.NotificationProvider
import com.pixnpunk.stock.data.workers.WorkerKeys.DEST_DIR
import com.pixnpunk.stock.data.workers.WorkerKeys.DOWNLOAD_URL
import com.pixnpunk.stock.data.workers.WorkerKeys.GAME_DATA_JSON
import com.pixnpunk.stock.domain.model.SearchState
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.storage.ArchiveExtractor
import com.pixnpunk.storage.di.storageModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.HttpHeaders
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
import java.io.File
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@RunWith(AndroidJUnit4::class)
class GameWorkersTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val logger = RecordingLogger()
    private val notifications = RecordingNotifications()
    private val repository = RecordingLocalRepository()

    private lateinit var root: File
    private lateinit var gameDir: File

    @Before
    fun setUp() {
        startKoin {
            androidContext(context)
            modules(storageModule)
        }
        root = File(context.filesDir, "workers-test").apply {
            deleteRecursively()
            mkdirs()
        }
        gameDir = File(root, "Game").apply { mkdirs() }
    }

    @After
    fun tearDown() {
        stopKoin()
        root.deleteRecursively()
    }

    @Test
    fun unpackWithoutForegroundPermissionSucceeds() = runBlocking {
        val game = "game".toByteArray()
        val archive = zip(File(gameDir, "game.zip"), mapOf("game.qsp" to game))
        val worker = unpackWorker(archive, deniedForeground)

        val result = worker.doWork()

        assertEquals(emptyList<Throwable>(), logger.errors)
        assertEquals(ListenableWorker.Result.success(), result)
        assertArrayEquals(game, File(gameDir, "game.qsp").readBytes())
        assertFalse(archive.exists())
        assertEquals(listOf(gameDir.toUri().toString()), repository.inserted.map { it.gameDirPath })
        assertTrue(logger.breadcrumbs.any { "Foreground service is not allowed" in it })
    }

    @Test
    fun cancellingUnpackDeletesGameDir() = runBlocking {
        val payload = Random(0).nextBytes(256 * 1024)
        val archive = zip(
            File(gameDir, "big.zip"),
            (0 until 200).associate { "file$it.bin" to payload }
        )
        val unpacking = CompletableDeferred<Unit>()
        notifications.onTitle = { if (it.startsWith("Unpacking")) unpacking.complete(Unit) }
        val worker = unpackWorker(archive, allowedForeground)

        val future = worker.startWork()
        withTimeout(10_000.milliseconds) { unpacking.await() }
        future.cancel(true)

        withTimeout(10_000.milliseconds) {
            while (gameDir.exists()) delay(50.milliseconds)
        }

        assertTrue(repository.inserted.isEmpty())
    }

    @Test
    fun downloadWithoutForegroundPermissionSucceeds() = runBlocking {
        val game = "game".toByteArray()
        val engine = MockEngine {
            respond(
                content = game,
                headers = headersOf(
                    HttpHeaders.ContentDisposition to listOf("attachment; filename=\"game.qsp\""),
                    HttpHeaders.ContentLength to listOf(game.size.toString())
                )
            )
        }
        val httpClient = HttpClient(engine) { install(HttpTimeout) }
        val worker = buildWorker(
            workDataOf(
                DOWNLOAD_URL to "https://example.org/download?id=1",
                DEST_DIR to gameDir.toUri().toString()
            ),
            deniedForeground
        ) { appContext, params ->
            DownloadGameWorker(appContext, params, logger, notifications, httpClient)
        }

        val result = worker.doWork()

        assertEquals(emptyList<Throwable>(), logger.errors)
        assertTrue(result is ListenableWorker.Result.Success)
        val filePath = result.outputData.getString("filePath")
        assertEquals(File(gameDir, "game.qsp").toUri().toString(), filePath)
        assertArrayEquals(game, File(gameDir, "game.qsp").readBytes())
        assertTrue(logger.breadcrumbs.any { "Foreground service is not allowed" in it })
    }

    private fun unpackWorker(archive: File, foregroundUpdater: ForegroundUpdater) =
        buildWorker(
            workDataOf(
                "filePath" to archive.toUri().toString(),
                "destDirPath" to gameDir.toUri().toString(),
                GAME_DATA_JSON to Json.encodeToString(GameEntry(title = "Test"))
            ),
            foregroundUpdater
        ) { appContext, params ->
            UnpackGameWorker(
                appContext, params, logger, repository, notifications,
                GlobalContext.get().get<ArchiveExtractor>()
            )
        }

    private inline fun <reified W : ListenableWorker> buildWorker(
        input: Data,
        foregroundUpdater: ForegroundUpdater,
        crossinline create: (Context, WorkerParameters) -> W
    ): W = TestListenableWorkerBuilder<W>(context, input)
        .setForegroundUpdater(foregroundUpdater)
        .setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ) = create(appContext, workerParameters)
        })
        .build()

    private fun zip(file: File, entries: Map<String, ByteArray>): File {
        ZipOutputStream(file.outputStream()).use { zip ->
            zip.setLevel(Deflater.NO_COMPRESSION)
            for ((path, bytes) in entries) {
                zip.putNextEntry(ZipEntry(path))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return file
    }

    private val deniedForeground = ForegroundUpdater { _, _, _ ->
        CallbackToFutureAdapter.getFuture {
            it.setException(IllegalStateException("Foreground service start not allowed"))
        }
    }

    private val allowedForeground = ForegroundUpdater { _, _, _ ->
        CallbackToFutureAdapter.getFuture { it.set(null) }
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
        var onTitle: (String) -> Unit = {}

        override fun createForegroundInfo(workId: UUID, title: String, progress: Float): ForegroundInfo {
            onTitle(title)
            val notification = Notification.Builder(context, "test")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .build()
            return ForegroundInfo(1, notification)
        }

        override fun showNotification(title: String, message: String) = Unit
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

        private fun unused(): Nothing = error("Not used by the workers")
    }
}
