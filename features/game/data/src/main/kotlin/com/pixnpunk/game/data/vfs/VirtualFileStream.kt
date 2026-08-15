package com.pixnpunk.game.data.vfs

internal interface VirtualFileStream : AutoCloseable {
    val size: Long
    fun seek(position: Long)
    fun read(buffer: ByteArray, offset: Int, length: Int): Int
}
