package com.pixnpunk.storage

import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.baseName
import com.anggrayudi.storage.file.fullName
import com.anggrayudi.storage.file.getRelativePath
import com.anggrayudi.storage.file.isWritable
import com.anggrayudi.storage.file.makeFile
import com.anggrayudi.storage.file.openOutputStream
import com.pixnpunk.dto.DownloadStatus
import com.pixnpunk.dto.ProgressState
import com.pixnpunk.common.runCatchingCancellable
import io.ktor.utils.io.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.IOException
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
}
