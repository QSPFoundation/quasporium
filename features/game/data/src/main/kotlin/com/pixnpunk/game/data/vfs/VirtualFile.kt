package com.pixnpunk.game.data.vfs

import com.anggrayudi.storage.file.MimeType
import com.pixnpunk.game.domain.model.VfsPath

internal interface VirtualFile {
    val absPath: VfsPath
    val uriPath: VfsPath
    val name: String
    val isDirectory: Boolean
    val isWritable: Boolean

    suspend fun readBytes(): Result<ByteArray>
    suspend fun writeBytes(data: ByteArray): Result<Unit>

    fun getChild(name: String): VirtualFile?
    fun getParent(): VirtualFile?

    fun makeDir(name: String): VirtualFile?
    fun makeFile(name: String, mimeType: String = MimeType.BINARY_FILE): VirtualFile?
    fun delete(): Boolean

    fun openStream(): Result<VirtualFileStream>
}
