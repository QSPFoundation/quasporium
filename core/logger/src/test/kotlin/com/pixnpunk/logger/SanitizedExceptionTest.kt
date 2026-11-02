package com.pixnpunk.logger

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.FileNotFoundException
import java.io.IOException

class SanitizedExceptionTest {

    @Test
    fun removesUrisAndStoragePaths() {
        val message = "Permission Denial: opening provider for content://com.android.externalstorage.documents" +
            "/tree/primary%3AGames/document/primary%3AGames%2Fsecret.qsp, " +
            "file:///storage/emulated/0/Games/a.sav (No such file) /data/user/0/com.pixnpunk/files/x " +
            "'/sdcard/My Games' and /mnt/media_rw/1234-5678/game.gam"

        assertEquals(
            "Permission Denial: opening provider for content://…, " +
                "file://… (No such file) <path> '<path> Games' and <path>",
            message.withoutPaths()
        )
    }

    @Test
    fun keepsTextWithoutPaths() {
        val message = "Unexpected end of stream at offset 42 (expected 1/2 of data)"

        assertEquals(message, message.withoutPaths())
    }

    @Test
    fun keepsClassStackAndCauseChain() {
        val cause = FileNotFoundException("/storage/emulated/0/game.qsp: open failed")
        val error = IllegalStateException("Cannot read content://provider/game", cause)

        val sanitized = error.sanitized()

        assertTrue(sanitized is SanitizedException)
        assertEquals("java.lang.IllegalStateException: Cannot read content://…", sanitized.message)
        assertArrayEquals(error.stackTrace, sanitized.stackTrace)
        val sanitizedCause = sanitized.cause!!
        assertEquals("java.io.FileNotFoundException: <path> open failed", sanitizedCause.message)
        assertArrayEquals(cause.stackTrace, sanitizedCause.stackTrace)
        assertNull(sanitizedCause.cause)
    }

    @Test
    fun sanitizesSuppressedExceptions() {
        val error = IOException("write failed")
        error.addSuppressed(IOException("close /data/data/app/files/save.sav"))

        val suppressed = error.sanitized().suppressed.single()

        assertEquals("java.io.IOException: close <path>", suppressed.message)
    }

    @Test
    fun handlesNullMessageAndCycles() {
        val first = RuntimeException()
        val second = RuntimeException("second", first)
        first.initCause(second)

        val sanitized = first.sanitized()

        assertEquals("java.lang.RuntimeException", sanitized.message)
        assertEquals("java.lang.RuntimeException: second", sanitized.cause!!.message)
        assertSame(sanitized, sanitized.cause!!.cause)
    }
}
