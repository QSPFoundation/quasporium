package com.pixnpunk.game.data.vfs

import com.pixnpunk.game.domain.model.VfsPath

internal interface VirtualFile {
    val path: VfsPath
    val name: String
    val isDirectory: Boolean
    val isWritable: Boolean

    suspend fun readBytes(): Result<ByteArray>
    suspend fun writeBytes(data: ByteArray): Result<Unit>

    fun getChild(name: String): VirtualFile?
    fun getParent(): VirtualFile?

    fun openStream(): Result<VirtualFileStream>
}
