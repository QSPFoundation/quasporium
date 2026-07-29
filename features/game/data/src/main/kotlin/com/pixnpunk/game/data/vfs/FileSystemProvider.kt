package com.pixnpunk.game.data.vfs

import com.pixnpunk.game.domain.model.VfsPath

internal interface FileSystemProvider {
    fun setGameRoot(rootPath: VfsPath)
    fun resolve(path: VfsPath): VirtualFile?
    fun resolveRelative(path: String): VirtualFile?
}
