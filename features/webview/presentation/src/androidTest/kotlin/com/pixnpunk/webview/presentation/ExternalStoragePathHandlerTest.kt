package com.pixnpunk.webview.presentation

import android.content.Context
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ExternalStoragePathHandlerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private lateinit var root: File
    private lateinit var handler: ExternalStoragePathHandler

    private companion object {
        val IMAGE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
    }

    @Before
    fun setUp() {
        root = File(context.filesDir, "path-handler-test").apply {
            deleteRecursively()
            mkdirs()
        }
        val gameDir = File(root, "game").apply { mkdirs() }
        File(gameDir, "images").mkdirs()
        File(gameDir, "images/logo.png").writeBytes(IMAGE)
        File(root, "outside.txt").writeText("secret")

        handler = ExternalStoragePathHandler(context, gameDir.toUri().toString())
    }

    @After
    fun tearDown() {
        root.deleteRecursively()
    }

    @Test
    fun servesFilesInsideTheGameFolder() {
        for (path in listOf("images/logo.png", "./images/logo.png", "images\\logo.png")) {
            val response = handler.handle(path)

            assertNotNull(path, response)
            assertArrayEquals(path, IMAGE, response!!.data.use { it.readBytes() })
        }
    }

    @Test
    fun doesNotServeFilesOutsideTheGameFolder() {
        val paths = listOf(
            "../outside.txt",
            "images/../../outside.txt",
            "..\\outside.txt",
            "%2e%2e/outside.txt",
            "images/missing.png"
        )
        for (path in paths) {
            assertNull(path, handler.handle(path))
        }
    }
}
