package com.pixnpunk.game.data.vfs.impl

import android.content.Context
import androidx.core.net.toUri
import com.anggrayudi.storage.extension.toDocumentFile
import com.pixnpunk.game.data.vfs.FileSystemProvider
import com.pixnpunk.game.data.vfs.VirtualFile
import com.pixnpunk.game.domain.model.VfsPath
import com.pixnpunk.utils.PathUtil.normalizeContentPath
import java.io.File
import java.util.concurrent.ConcurrentHashMap

internal class FileSystemProviderImpl(
    private val context: Context
) : FileSystemProvider {

    private var gameRoot: VirtualFile? = null
    private val resolveCache = ConcurrentHashMap<String, VirtualFile>()

    override fun setGameRoot(rootPath: VfsPath) {
        resolveCache.clear()
        val resolved = resolve(rootPath) ?: return

        // TODO GETPARENT IS NOT WORK. NEED TO REWRITE SYSTEM!
        gameRoot = if (resolved.isDirectory) resolved else resolved.getParent()
    }

    override fun resolve(path: VfsPath): VirtualFile? {
        val uriStr = path.value
        return when {
            uriStr.startsWith("content://") -> {
                val doc = uriStr.toUri().toDocumentFile(context)
                if (doc != null && doc.exists()) DocumentVirtualFile(context, doc) else null
            }

            uriStr.startsWith("file:") -> {
                val file = File(uriStr.toUri().path!!)
                if (file.exists()) DirectVirtualFile(file) else null
            }

            else -> {
                val file = File(uriStr)
                if (file.exists()) DirectVirtualFile(file) else null
            }
        }
    }

    override fun resolveRelative(path: String): VirtualFile? {
        val root = gameRoot ?: return null
        val cleanPath = path.normalizeContentPath()

        resolveCache[cleanPath]?.let { return it }
        val resolvedChild = root.getChild(cleanPath)

        if (resolvedChild != null) {
            resolveCache[cleanPath] = resolvedChild
        }

        return resolvedChild
    }
}
