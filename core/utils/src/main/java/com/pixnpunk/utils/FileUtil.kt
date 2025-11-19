package com.pixnpunk.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.extension.openInputStream
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.file.DocumentFileCompat.fromFullPath
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.isWritable
import com.anggrayudi.storage.file.makeFile
import com.anggrayudi.storage.file.openOutputStream
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.IOException
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
    fun createSpecialFiles(path: String) {
        val gameDir = fromFullPath(
            context = get(),
            fullPath = path,
            requiresWriteAccess = true
        )
        if (!gameDir.isWritableDir()) return

        gameDir.makeFile(get(), NOMEDIA_FILENAME, MimeType.TEXT, CreateMode.SKIP_IF_EXISTS)
        gameDir.makeFile(get(), NOSEARCH_FILENAME, MimeType.TEXT, CreateMode.SKIP_IF_EXISTS)
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun writeFileToDir(
        fileChannel: ByteReadChannel,
        fileName: String,
        destDirPath: String
    ) {
        val gameDir = fromFullPath(
            context = get(),
            fullPath = destDirPath,
            requiresWriteAccess = true
        )
        if (!gameDir.isWritableDir()) return
        val destFile = gameDir.makeFile(get(), fileName, mode = CreateMode.SKIP_IF_EXISTS)
        if (!destFile.isWritableFile()) return
        fileChannel.copyTo(destFile.openOutputStream(get())!!)
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
}