package com.pixnpunk.storage

import android.content.Context
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pixnpunk.dto.ProgressState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@RunWith(AndroidJUnit4::class)
class SevenZipArchiveExtractorTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val extractor = SevenZipArchiveExtractor(context)

    private lateinit var root: File
    private lateinit var dest: File

    private companion object {
        const val FILE_COUNT = 200
    }

    private suspend fun unpack(
        archive: File,
        onProgressUpdate: suspend (ProgressState) -> Unit = {}
    ) = extractor.unpack(
        archive.toUri().toString(),
        dest.toUri().toString(),
        onProgressUpdate
    )

    private fun zip(
        name: String,
        entries: Map<String, ByteArray>
    ): File {
        val file = File(root, name)
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

    @Before
    fun setUp() {
        root = File(context.cacheDir, "unpack-test").apply {
            deleteRecursively()
            mkdirs()
        }
        dest = File(root, "dest").apply { mkdirs() }
    }

    @After
    fun tearDown() {
        root.deleteRecursively()
    }

    @Test
    fun unpacksFilesAndFolders() = runBlocking {
        val game = "game".toByteArray()
        val image = Random(0).nextBytes(1024)
        val archive = zip("game.zip", mapOf("game.qsp" to game, "img/logo.png" to image))

        assertTrue(unpack(archive).isSuccess)

        assertArrayEquals(game, File(dest, "game.qsp").readBytes())
        assertArrayEquals(image, File(dest, "img/logo.png").readBytes())
    }

    @Test
    fun rejectsEntriesOutsideDestination() = runBlocking {
        for (path in listOf("../evil.txt", "a/../../evil.txt")) {
            val archive = zip("evil.zip", mapOf("game.qsp" to byteArrayOf(1), path to byteArrayOf(2)))

            assertTrue(path, unpack(archive).isFailure)
            assertFalse(path, File(root, "evil.txt").exists())
        }
    }

    @Test
    fun cancellingStopsExtraction() = runBlocking {
        val payload = Random(0).nextBytes(256 * 1024)
        val archive = zip("big.zip", (0 until FILE_COUNT).associate { "file$it.bin" to payload })

        lateinit var job: Job
        job = launch(Dispatchers.Default) {
            unpack(archive) { job.cancel() }
        }
        withTimeout(10_000.milliseconds) { job.join() }

        assertTrue(job.isCancelled)
        val extracted = dest.listFiles().orEmpty().size
        assertTrue("extracted $extracted of $FILE_COUNT", extracted < FILE_COUNT)
    }
}
