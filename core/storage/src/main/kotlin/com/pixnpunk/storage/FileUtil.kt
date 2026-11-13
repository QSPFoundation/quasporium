package com.pixnpunk.storage

import android.content.Context
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object FileUtil : KoinComponent {
    private const val NOMEDIA_FILENAME: String = ".nomedia"
    private const val NOSEARCH_FILENAME: String = ".nosearch"

    @ExperimentalContracts
    fun DocumentFile?.isWritableFile(context: Context = get()): Boolean {
        contract {
            returns(true) implies (this@isWritableFile != null)
        }
        if (this == null) return false
        return this.exists() && this.isFile && this.isWritable(context)
    }

    @ExperimentalContracts
    fun DocumentFile?.isWritableDir(context: Context = get()): Boolean {
        contract {
            returns(true) implies (this@isWritableDir != null)
        }
        if (this == null) return false
        return this.exists() && this.isDirectory && this.isWritable(context)
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
}
