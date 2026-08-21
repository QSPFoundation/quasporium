package com.pixnpunk.game.data.vfs.impl

import androidx.core.net.toUri
import com.anggrayudi.storage.file.child
import com.anggrayudi.storage.file.forceDelete
import com.pixnpunk.game.data.vfs.VirtualFile
import com.pixnpunk.game.data.vfs.VirtualFileStream
import com.pixnpunk.game.domain.model.VfsPath
import java.io.File

internal class DirectVirtualFile(
    private val file: File
) : VirtualFile {

    override val absPath: VfsPath get() = VfsPath(file.absolutePath)
    override val uriPath: VfsPath get() = VfsPath(file.toURI().toString())
    override val name: String get() = file.name
    override val isDirectory: Boolean get() = file.isDirectory
    override val isWritable: Boolean get() = file.canWrite()

    override suspend fun readBytes(): Result<ByteArray> = runCatching {
        file.readBytes()
    }

    override suspend fun writeBytes(data: ByteArray): Result<Unit> = runCatching {
        file.writeBytes(data)
    }

    override fun getChild(name: String): VirtualFile? =
        file.child(name).let {
            if (it.exists()) DirectVirtualFile(it) else null
        }

    override fun getParent(): VirtualFile? =
        file.parentFile.let {
            if (it != null && it.exists()) DirectVirtualFile(it) else null
        }

    override fun makeDir(name: String): VirtualFile? {
        val newDir = File(file, name)
        if (newDir.exists() && newDir.isDirectory) return DirectVirtualFile(newDir)
        return if (newDir.mkdirs()) DirectVirtualFile(newDir) else null
    }

    override fun makeFile(name: String, mimeType: String): VirtualFile? {
        val newFile = File(file, name)
        if (newFile.exists() && newFile.isFile) return DirectVirtualFile(newFile)

        return runCatching {
            if (newFile.createNewFile()) DirectVirtualFile(newFile) else null
        }.getOrNull()
    }

    override fun delete(): Boolean = file.forceDelete()

    override fun openStream(): Result<VirtualFileStream> {
        TODO("Not yet implemented")
    }
}