package com.pixnpunk.game.data.vfs.impl

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.extension.openInputStream
import com.anggrayudi.storage.extension.openOutputStream
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.file.child
import com.anggrayudi.storage.file.findParent
import com.anggrayudi.storage.file.forceDelete
import com.anggrayudi.storage.file.getAbsolutePath
import com.anggrayudi.storage.file.isWritable
import com.anggrayudi.storage.file.makeFile
import com.anggrayudi.storage.file.makeFolder
import com.pixnpunk.game.data.vfs.VirtualFile
import com.pixnpunk.game.data.vfs.VirtualFileStream
import com.pixnpunk.game.domain.model.VfsPath

internal class DocumentVirtualFile(
    private val context: Context,
    private val docFile: DocumentFile
) : VirtualFile {

    override val absPath: VfsPath get() = VfsPath(docFile.getAbsolutePath(context))
    override val uriPath: VfsPath get() = VfsPath(docFile.uri.toString())
    override val name: String get() = docFile.name ?: "unknown"
    override val isDirectory: Boolean get() = docFile.isDirectory
    override val isWritable: Boolean get() = docFile.isWritable(context)

    override suspend fun readBytes(): Result<ByteArray> =
        runCatching {
            docFile.uri.openInputStream(context)?.use { it.readBytes() }
                ?: throw IllegalStateException("Cannot open InputStream for $absPath")
        }

    override suspend fun writeBytes(data: ByteArray): Result<Unit> =
        runCatching {
            docFile.uri.openOutputStream(context, false)?.use { it.write(data) }
                ?: throw IllegalStateException("Cannot open OutputStream for $absPath")
        }

    override fun getChild(name: String): VirtualFile? =
        DocumentVirtualFile(
            context,
            docFile.child(context, name) ?: return null
        )

    override fun getParent(): VirtualFile? =
        DocumentVirtualFile(
            context,
            docFile.findParent(context) ?: return null
        )

    override fun makeDir(name: String): VirtualFile? =
        DocumentVirtualFile(
            context = context,
            docFile.makeFolder(
                context = context,
                name = name,
                mode = CreateMode.REUSE
            ) ?: return null
        )

    override fun makeFile(
        name: String,
        mimeType: String
    ): VirtualFile? =
        DocumentVirtualFile(
            context = context,
            docFile.makeFile(
                context = context,
                name = name,
                mimeType = mimeType,
                mode = CreateMode.REUSE
            ) ?: return null
        )

    override fun delete(): Boolean = docFile.forceDelete(context = context)

    override fun openStream(): Result<VirtualFileStream> {
        TODO("Not yet implemented")
    }
}