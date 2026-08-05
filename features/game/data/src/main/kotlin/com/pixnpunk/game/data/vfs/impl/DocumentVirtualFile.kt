package com.pixnpunk.game.data.vfs.impl

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.file.child
import com.anggrayudi.storage.file.findParent
import com.anggrayudi.storage.file.forceDelete
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

    override val path: VfsPath get() = VfsPath(docFile.uri.toString())
    override val name: String get() = docFile.name ?: "unknown"
    override val isDirectory: Boolean get() = docFile.isDirectory
    override val isWritable: Boolean get() = docFile.isWritable(context)

    override suspend fun readBytes(): Result<ByteArray> = runCatching {
        context.contentResolver.openInputStream(docFile.uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Cannot open InputStream for $path")
    }

    override suspend fun writeBytes(data: ByteArray): Result<Unit> = runCatching {
        context.contentResolver.openOutputStream(docFile.uri, "w")?.use { it.write(data) }
            ?: throw IllegalStateException("Cannot open OutputStream for $path")
    }

    override fun getChild(name: String): VirtualFile? {
        val segments = name.split("/")
        var current: DocumentFile = docFile

        for (segment in segments) {
            if (segment.isEmpty()) continue
            current = current.child(context, segment) ?: return null
        }

        return DocumentVirtualFile(context, docFile.child(context, name) ?: return null)
    }

    override fun getParent(): VirtualFile? {
        val parent = docFile.findParent(context)
        return if (parent != null) DocumentVirtualFile(context, parent) else null
    }

    override fun makeDir(name: String): VirtualFile? =
        DocumentVirtualFile(
            context = context,
            docFile.makeFolder(
                context = context,
                name = name,
                mode = CreateMode.REUSE
            ) ?: return null
        )

    override fun makeFile(name: String, mimeType: String): VirtualFile? =
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