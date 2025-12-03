package com.pixnpunk.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import net.sf.sevenzipjbinding.IInStream
import net.sf.sevenzipjbinding.ISeekableStream
import net.sf.sevenzipjbinding.SevenZipException
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.EOFException
import java.io.FileInputStream
import java.io.IOException

internal class DocumentFileRandomInStream(
    private val singleFileUri: Uri
) : IInStream, KoinComponent {
    private val contentResolver: ContentResolver = get<Context>().contentResolver
    private var pfdInput: ParcelFileDescriptor? = null
    private var stream: FileInputStream? = null
    private var streamSize: Long = 0
    private var position: Long = 0

    init {
        try {
            openUri()
            streamSize = stream!!.getChannel().size()
        } catch (e: IOException) {
            println("Error: $e")
        }
    }

    @Throws(IOException::class)
    private fun openUri() {
        if (stream != null) stream?.close()
        if (pfdInput != null) pfdInput?.close()
        pfdInput = contentResolver.openFileDescriptor(singleFileUri, "r")
        if (pfdInput != null) stream = FileInputStream(pfdInput?.fileDescriptor)
    }

    @Throws(SevenZipException::class)
    override fun seek(offset: Long, seekOrigin: Int): Long {
        val seekDelta = when (seekOrigin) {
            ISeekableStream.SEEK_CUR -> offset
            ISeekableStream.SEEK_SET -> offset - position
            ISeekableStream.SEEK_END -> streamSize + offset - position
            else -> 0L
        }
        if (position + seekDelta > streamSize) position = streamSize
        if (seekDelta != 0L) {
            try {
                if (seekDelta < 0) {
                    openUri()
                    skipNBytes(position + seekDelta)
                } else {
                    skipNBytes(seekDelta)
                }
            } catch (e: IOException) {
                throw SevenZipException(e)
            }
        }
        position += seekDelta
        return position
    }

    // Taken from Java14's InputStream
    // as basic skip is limited by the size of its buffer
    @Throws(IOException::class)
    private fun skipNBytes(n: Long) {
        var n = n
        if (n > 0) {
            val ns = stream!!.skip(n)
            if (ns < n) { // skipped too few bytes
                // adjust number to skip
                n -= ns
                // read until requested number skipped or EOS reached
                while (n > 0 && stream!!.read() != -1) {
                    n--
                }
                // if not enough skipped, then EOFE
                if (n != 0L) {
                    throw EOFException()
                }
            } else if (ns != n) { // skipped negative or too many bytes
                throw IOException("Unable to skip exactly")
            }
        }
    }

    @Throws(SevenZipException::class)
    override fun read(bytes: ByteArray): Int {
        try {
            var result = stream!!.read(bytes)
            position += result.toLong()
            if (result != bytes.size)
                println("diff ${bytes.size} expected; $result read")
            if (result < 0) result = 0
            return result
        } catch (e: IOException) {
            throw SevenZipException(e)
        }
    }

    @Throws(IOException::class)
    override fun close() {
        stream!!.close()
        pfdInput!!.close()
    }
}
