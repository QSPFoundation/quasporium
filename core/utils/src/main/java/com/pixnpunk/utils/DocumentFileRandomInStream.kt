package com.pixnpunk.utils

import android.content.ContentResolver
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.io.files.FileNotFoundException
import net.sf.sevenzipjbinding.IInStream
import net.sf.sevenzipjbinding.ISeekableStream
import net.sf.sevenzipjbinding.SevenZipException
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer.wrap
import java.nio.channels.FileChannel

internal class DocumentFileRandomInStream(
    private val singleFileUri: Uri,
    private val contentResolver: ContentResolver
) : IInStream {
    private var pfdInput: ParcelFileDescriptor? = null
    private var fileChannel: FileChannel? = null
    private var streamSize: Long = 0

    init {
        try {
            openChannel()
        } catch (e: IOException) {
            close()
            throw e
        }
    }

    @Throws(FileNotFoundException::class)
    private fun openChannel() {
        val pfd = contentResolver.openFileDescriptor(singleFileUri, "r")
            ?: throw FileNotFoundException("Could not open file descriptor for $singleFileUri")

        pfdInput = pfd
        fileChannel = FileInputStream(pfd.fileDescriptor).channel
        streamSize = if (pfd.statSize != -1L) pfd.statSize else fileChannel?.size() ?: 0
    }

    @Throws(SevenZipException::class)
    override fun seek(offset: Long, seekOrigin: Int): Long {
        val channel = fileChannel ?: throw SevenZipException("Stream is closed")

        try {
            val currentPos = channel.position()
            var newPos = when (seekOrigin) {
                ISeekableStream.SEEK_SET -> offset
                ISeekableStream.SEEK_CUR -> currentPos + offset
                ISeekableStream.SEEK_END -> streamSize + offset
                else -> throw SevenZipException("Unknown seek origin: $seekOrigin")
            }

            if (newPos > streamSize) newPos = streamSize
            if (newPos < 0) newPos = 0

            channel.position(newPos)
            return newPos
        } catch (e: IOException) {
            throw SevenZipException("Error seeking to offset $offset", e)
        }
    }

    @Throws(SevenZipException::class)
    override fun read(bytes: ByteArray): Int {
        if (bytes.isEmpty()) return 0
        val channel = fileChannel ?: throw SevenZipException("Stream is closed")
        try {
            val bytesRead = channel.read(wrap(bytes))
            return if (bytesRead == -1) 0 else bytesRead
        } catch (e: IOException) {
            throw SevenZipException("Error reading stream", e)
        }
    }

    @Throws(IOException::class)
    override fun close() {
        try {
            fileChannel?.close()
        } finally {
            pfdInput?.close()
            fileChannel = null
            pfdInput = null
        }
    }
}
