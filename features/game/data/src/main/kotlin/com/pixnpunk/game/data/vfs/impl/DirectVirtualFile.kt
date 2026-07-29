package com.pixnpunk.game.data.vfs.impl

import com.pixnpunk.game.data.vfs.VirtualFile
import com.pixnpunk.game.data.vfs.VirtualFileStream
import com.pixnpunk.game.domain.model.VfsPath
import java.io.File

internal class DirectVirtualFile(
    private val file: File
) : VirtualFile {
    override val path: VfsPath get() = VfsPath(file.absolutePath)
    override val name: String get() = file.name
    override val isDirectory: Boolean get() = file.isDirectory
    override val isWritable: Boolean get() = file.canWrite()

    override suspend fun readBytes(): Result<ByteArray> = runCatching {
        file.readBytes()
    }

    override suspend fun writeBytes(data: ByteArray): Result<Unit> = runCatching {
        file.writeBytes(data)
    }

    override fun getChild(name: String): VirtualFile? {
        val child = File(file, name)
        return if (child.exists()) DirectVirtualFile(child) else null
    }

    override fun getParent(): VirtualFile? {
        val parent = file.parentFile
        return if (parent != null && parent.exists()) DirectVirtualFile(parent) else null
    }

    override fun openStream(): Result<VirtualFileStream> {
        TODO("Not yet implemented")
    }
}