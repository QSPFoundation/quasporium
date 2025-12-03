package com.pixnpunk.utils

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.extension.openInputStream
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.MimeType.getExtensionFromFileName
import com.anggrayudi.storage.file.getRelativePath
import com.anggrayudi.storage.file.isWritable
import com.anggrayudi.storage.file.makeFile
import com.anggrayudi.storage.file.openOutputStream
import com.pixnpunk.dto.DownloadStatus
import com.pixnpunk.dto.ProgressState
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.flow.FlowCollector
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
import java.text.DecimalFormat
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.log10
import kotlin.math.pow

object FileUtil : KoinComponent {
    @ExperimentalContracts
    fun DocumentFile?.isWritableFile(): Boolean {
        contract {
            returns() implies (this@isWritableFile != null)
        }
        if (this == null) return false
        return this.exists() && this.isFile && this.isWritable(get())
    }

    @ExperimentalContracts
    fun DocumentFile?.isWritableDir(): Boolean {
        contract {
            returns() implies (this@isWritableDir != null)
        }
        if (this == null) return false
        return this.exists() && this.isDirectory && this.isWritable(get())
    }

    fun String.receiveRelPath() =
        fromUri(get(), this.toUri())?.getRelativePath(get()) ?: ""

    fun formatFileSize(size: Long, numCountInfo: Int): String {
        if (size <= 0) {
            return "0"
        }
        var units = arrayOfNulls<String>(0)
        if (numCountInfo == 1000) {
            units = arrayOf("B", "KB", "MB", "GB", "TB")
        } else if (numCountInfo == 1024) {
            units = arrayOf("B", "KiB", "MiB", "GiB", "TiB")
        }
        val digitGroups = (log10(size.toDouble()) / log10(numCountInfo.toDouble())).toInt()
        return (DecimalFormat("#,##0.#").format(
            size / numCountInfo.toDouble().pow(digitGroups.toDouble())
        ) + " " + units[digitGroups])
    }

    const val NOMEDIA_FILENAME: String = ".nomedia"
    const val NOSEARCH_FILENAME: String = ".nosearch"

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
        progress: FlowCollector<ProgressState>
    ): Result<String> = runCatching {
        progress.emit(
            ProgressState(
                progress = 0F,
                status = DownloadStatus.WRITE_FILE,
                time = ""
            )
        )
        val gameDir = fromUri(get(), destDirPath.toUri())
        if (!gameDir.isWritableDir()) return Result.failure(NullPointerException())
        val destFile = gameDir.makeFile(get(), fileName, mode = CreateMode.SKIP_IF_EXISTS)
        if (!destFile.isWritableFile()) return Result.failure(NullPointerException())
        fileChannel.copyTo(destFile.openOutputStream(get())!!)
        progress.emit(
            ProgressState(
                progress = 1.0F,
                status = DownloadStatus.WRITE_FILE,
                time = ""
            )
        )
        return Result.success(destFile.uri.toString())
    }

    fun Uri.readFileContents(): ByteArray? {
        return runCatching {
            this.openInputStream(get()).use { it?.readBytes() ?: throw NullPointerException() }
        }.getOrElse {
//            Firebase.crashlytics.log("Error reading file: $this | Throwable: $it")
            null
        }
    }

    fun Uri.writeFileContents(dataToWrite: ByteArray) {
        get<Context>().contentResolver
            .runCatching {
                openOutputStream(this@writeFileContents).use { out ->
                    out?.write(dataToWrite) ?: throw IOException("Input is NULL!")
                }
            }
            .onFailure {
//                Firebase.crashlytics.log("Error writing in file: $this | Throwable: $it")
            }
    }

    suspend fun unpackFileToDir(
        filePath: String,
        destDirPath: String,
        progress: FlowCollector<ProgressState>
    ) = runCatching {
        val fileUri = filePath.toUri()
        DocumentFileRandomInStream(fileUri).use {
            SevenZip.openInArchive(null, it).use { inArchive ->
                inArchive.extract(
                    null,
                    false,
                    ArchiveExtractCallback(
                        destDirPath.toUri().toDocumentFile(get())!!,
                        inArchive,
                        progress
                    )
                )
            }
        }
        fileUri.toDocumentFile(get())?.delete()
        Unit
    }

    private class ArchiveExtractCallback(
        private val targetDir: DocumentFile,
        private val inArchive: IInArchive,
        private val progress: FlowCollector<ProgressState>
    ) : IArchiveExtractCallback {
        private var stream: SequentialOutStream? = null

        override fun getStream(
            index: Int,
            extractAskMode: ExtractAskMode?
        ): ISequentialOutStream? {
            val isFolder = inArchive.getProperty(index, PropID.IS_FOLDER) as? Boolean ?: false
            val pathString = inArchive.getProperty(index, PropID.PATH) as? String ?: return null

            if (isFolder) { return null }

            val file = targetDir.makeFile(
                get(),
                pathString,
                getExtensionFromFileName(pathString)
            )

            stream = SequentialOutStream(file!!.openOutputStream(get())!!)
            return stream
        }

        override fun prepareOperation(extractAskMode: ExtractAskMode?) {
            //
        }

        override fun setOperationResult(extractOperationResult: ExtractOperationResult?) {
            try {
                stream?.close()
                stream = null
            } catch (e: IOException) {
                throw SevenZipException(e)
            }

            if (extractOperationResult != ExtractOperationResult.OK) {
                throw SevenZipException(extractOperationResult.toString())
            }
        }

        override fun setTotal(total: Long) {
            //
        }

        override fun setCompleted(complete: Long) {
            //
        }
    }

    private class SequentialOutStream(private val out: OutputStream) : ISequentialOutStream {
        @Throws(SevenZipException::class)
        override fun write(data: ByteArray): Int {
            try {
                out.write(data)
            } catch (e: IOException) {
                throw SevenZipException(e)
            }
            return data.size
        }

        @Throws(IOException::class)
        fun close() {
            out.close()
        }
    }
}