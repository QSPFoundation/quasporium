package com.pixnpunk.storage

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.MimeType.getExtensionFromFileName
import com.anggrayudi.storage.file.baseName
import com.anggrayudi.storage.file.fullName
import com.anggrayudi.storage.file.getRelativePath
import com.anggrayudi.storage.file.isWritable
import com.anggrayudi.storage.file.makeFile
import com.anggrayudi.storage.file.openOutputStream
import com.pixnpunk.dto.DownloadStatus
import com.pixnpunk.dto.ProgressState
import com.pixnpunk.common.PathUtil.toSafeRelativePath
import com.pixnpunk.common.TimeUtil.formatEta
import com.pixnpunk.common.runCatchingCancellable
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.sf.sevenzipjbinding.ExtractAskMode
import net.sf.sevenzipjbinding.ExtractOperationResult
import net.sf.sevenzipjbinding.IArchiveExtractCallback
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.ISequentialOutStream
import net.sf.sevenzipjbinding.PropID
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.IOException
import java.io.OutputStream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object FileUtil : KoinComponent {
    private const val NOMEDIA_FILENAME: String = ".nomedia"
    private const val NOSEARCH_FILENAME: String = ".nosearch"

    @ExperimentalContracts
    fun DocumentFile?.isWritableFile(): Boolean {
        contract {
            returns(true) implies (this@isWritableFile != null)
        }
        if (this == null) return false
        return this.exists() && this.isFile && this.isWritable(get())
    }

    @ExperimentalContracts
    fun DocumentFile?.isWritableDir(): Boolean {
        contract {
            returns(true) implies (this@isWritableDir != null)
        }
        if (this == null) return false
        return this.exists() && this.isDirectory && this.isWritable(get())
    }

    fun String.receiveBaseName() =
        fromUri(get(), this.toUri())?.baseName ?: ""

    fun String.receiveFullName() =
        fromUri(get(), this.toUri())?.fullName ?: ""

    fun String.receiveDirRelPath() =
        fromUri(get(), this.toUri())?.let {
            "${it.getRelativePath(get())}/${it.baseName}"
        } ?: ""

    fun String.receiveFileRelPath() =
        fromUri(get(), this.toUri())?.let {
            "${it.getRelativePath(get())}/${it.fullName}"
        } ?: ""

    fun String.removeFileByPath() =
        this.toUri().toDocumentFile(get())?.delete() ?: false

    @OptIn(ExperimentalContracts::class)
    fun createSpecialFiles(dirPath: String) {
        val gameDir = fromUri(get(), dirPath.toUri())
        if (!gameDir.isWritableDir()) return

        gameDir.makeFile(get(), NOMEDIA_FILENAME, MimeType.TEXT, CreateMode.SKIP_IF_EXISTS)
        gameDir.makeFile(get(), NOSEARCH_FILENAME, MimeType.TEXT, CreateMode.SKIP_IF_EXISTS)
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun writeFileToDir(
        fileChannel: ByteReadChannel,
        fileName: String,
        destDirPath: String,
        totalSize: Long,
        onProgressUpdate: suspend (state: ProgressState) -> Unit
    ): Result<String> = runCatchingCancellable {
        val gameDir = fromUri(get(), destDirPath.toUri())
        if (!gameDir.isWritableDir()) {
            val reason =
                if (gameDir == null) "Dir is null (URI invalid/no permission)" else "No write access to dir"
            return Result.failure(IOException("DestDir error: $reason"))
        }

        val destFile = gameDir.makeFile(get(), fileName, mode = CreateMode.SKIP_IF_EXISTS)
        if (!destFile.isWritableFile()) {
            return Result.failure(
                IOException(
                    "DestFile error: ${
                        when {
                            destFile == null -> "makeFile returned null (invalid chars in name: '$fileName'?)"
                            !destFile.exists() -> "File doesn't exist after creation"
                            !destFile.isFile -> "Target is a directory, not a file"
                            else -> "No write permission to file"
                        }
                    }. Name: $fileName"
                )
            )
        }

        val startTime = System.currentTimeMillis()
        var lastEmitTime = 0L

        val outputStream = destFile.openOutputStream(get())
            ?: return Result.failure(IOException("Failed to open outputStream for $fileName"))

        outputStream.use { output ->
            val buffer = ByteArray(8 * 1024)
            var bytesCopied = 0L

            while (!fileChannel.isClosedForRead) {
                val bytesRead = fileChannel.readAvailable(buffer)
                if (bytesRead == -1) break
                output.write(buffer, 0, bytesRead)
                bytesCopied += bytesRead

                val currentTime = System.currentTimeMillis()
                if (totalSize > 0 && currentTime - lastEmitTime >= 500) {
                    val timeElapsed = currentTime - startTime
                    val speed = if (timeElapsed > 0) bytesCopied.toDouble() / timeElapsed else 0.0
                    val remainingBytes = totalSize - bytesCopied
                    val remainingMs = if (speed > 0) (remainingBytes / speed).toLong() else 0L

                    val seconds = (remainingMs / 1000) % 60
                    val minutes = (remainingMs / (1000 * 60)) % 60

                    onProgressUpdate(
                        ProgressState(
                            progress = bytesCopied.toFloat() / totalSize,
                            status = DownloadStatus.WRITE_FILE,
                            time = "%02d:%02d".format(minutes, seconds)
                        )
                    )

                    lastEmitTime = currentTime
                }
            }
        }

        return Result.success(destFile.uri.toString())
    }

    suspend fun unpackFileToDir(
        filePath: String,
        destDirPath: String,
        onProgressUpdate: suspend (state: ProgressState) -> Unit
    ): Result<Unit> = runCatchingCancellable {
        DocumentFileRandomInStream(
            singleFileUri = filePath.toUri(),
            contentResolver = get<Context>().contentResolver
        ).use { randomInStream ->
            withContext(Dispatchers.IO) {
                SevenZip.openInArchive(null, randomInStream).use { inArchive ->
                    try {
                        inArchive.extract(
                            null,
                            false,
                            ArchiveExtractCallback(
                                context = get(),
                                targetDir = destDirPath.toUri().toDocumentFile(get())!!,
                                inArchive = inArchive,
                                onProgressUpdate = onProgressUpdate,
                                coroutineScope = this
                            )
                        )
                    } catch (e: SevenZipException) {
                        ensureActive()
                        throw e
                    }
                }
            }
        }
    }

    private class ArchiveExtractCallback(
        private val context: Context,
        private val targetDir: DocumentFile,
        private val inArchive: IInArchive,
        private val onProgressUpdate: suspend (state: ProgressState) -> Unit,
        private val coroutineScope: CoroutineScope
    ) : IArchiveExtractCallback {
        private var stream: SequentialOutStream? = null
        private var totalSize: Long = 0L

        private var startTime: Long = 0L
        private var lastUpdateTime: Long = 0L

        private companion object {
            const val UPDATE_INTERVAL_MS = 200L
        }

        private fun throwIfCancelled() {
            if (!coroutineScope.isActive) throw SevenZipException("Extraction cancelled")
        }

        override fun getStream(
            index: Int,
            extractAskMode: ExtractAskMode?
        ): ISequentialOutStream? {
            if (extractAskMode != ExtractAskMode.EXTRACT) return null
            throwIfCancelled()

            val isFolder = inArchive.getProperty(index, PropID.IS_FOLDER) as? Boolean ?: false
            if (isFolder) return null

            val rawPath = inArchive.getProperty(index, PropID.PATH) as? String ?: return null
            val safePath = rawPath.toSafeRelativePath()
                ?: throw SevenZipException("Unsafe archive entry path: $rawPath")

            val file = targetDir.makeFile(
                get(),
                safePath,
                getExtensionFromFileName(safePath),
                mode = CreateMode.REUSE
            ) ?: throw IOException("Could not create file: $safePath")

            val outputStream = file.openOutputStream(context)
                ?: throw IOException("Could not open stream for: $safePath")

            stream = SequentialOutStream(outputStream)
            return stream
        }

        override fun prepareOperation(extractAskMode: ExtractAskMode?) {}

        override fun setOperationResult(extractOperationResult: ExtractOperationResult?) {
            try {
                stream?.close()
            } catch (e: IOException) {
                throw SevenZipException(e)
            } finally {
                stream = null
            }

            if (extractOperationResult != ExtractOperationResult.OK) {
                throw SevenZipException(extractOperationResult.toString())
            }
        }

        override fun setTotal(total: Long) {
            this.totalSize = total
        }

        override fun setCompleted(complete: Long) {
            throwIfCancelled()
            val currentTime = System.currentTimeMillis()

            if (startTime == 0L) startTime = currentTime

            if (currentTime - lastUpdateTime > UPDATE_INTERVAL_MS || complete == totalSize) {
                lastUpdateTime = currentTime

                val timeElapsed = currentTime - startTime
                val etaString = if (timeElapsed > 0 && complete > 0 && totalSize > 0) {
                    val speed = complete.toDouble() / timeElapsed
                    val remainingBytes = totalSize - complete
                    (remainingBytes / speed).toLong().formatEta()
                } else {
                    if (complete == totalSize) "00:00" else "--:--"
                }

                coroutineScope.launch {
                    onProgressUpdate(
                        ProgressState(
                            progress =
                                if (totalSize > 0) complete.toFloat() / totalSize.toFloat() else 0f,
                            status = DownloadStatus.UNPACK_FILE,
                            time = etaString
                        )
                    )
                }
            }
        }
    }

    private class SequentialOutStream(private val out: OutputStream) : ISequentialOutStream {
        @Throws(SevenZipException::class)
        override fun write(data: ByteArray) =
            try {
                out.write(data)
                data.size
            } catch (e: IOException) {
                throw SevenZipException(e)
            }

        @Throws(IOException::class)
        fun close() = out.close()
    }
}
