package com.pixnpunk.storage

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.child
import com.anggrayudi.storage.file.getAbsolutePath
import com.pixnpunk.common.PathUtil.toRootRelativePath
import com.pixnpunk.storage.FileUtil.isWritableDir
import com.pixnpunk.storage.FileUtil.isWritableFile
import java.io.File
import kotlin.contracts.ExperimentalContracts

object StoragePathUtil {
    fun String.absToRelPath(context: Context, rootDir: DocumentFile) =
        if (!this.startsWith('/')) {
            this
        } else {
            val rootDirPathAsFile = File(rootDir.getAbsolutePath(context))
            File(this).toRelativeString(rootDirPathAsFile)
        }

    fun String.pathToFile(context: Context, rootDir: DocumentFile): DocumentFile? =
        rootDir.child(
            context = context,
            path = toRootRelativePath(
                rootDir.getAbsolutePath(context)
            ) ?: return null
        )

    @OptIn(ExperimentalContracts::class)
    fun String.getImageUriFromPath(
        context: Context,
        rootDirUriBox: String
    ): Uri {
        val rootDir = rootDirUriBox.toUri().toDocumentFile(context)
        if (!rootDir.isWritableDir()) return Uri.EMPTY
        val relPath = this.toUri().path
        if (relPath.isNullOrEmpty()) return Uri.EMPTY
        val imageFile = relPath.pathToFile(context, rootDir)
        if (!imageFile.isWritableFile()) return Uri.EMPTY
        return imageFile.uri
    }
}
