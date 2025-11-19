package com.pixnpunk.utils

import android.content.Context
import android.os.Build
import android.provider.DocumentsContract
import android.provider.DocumentsContract.getDocumentMetadata
import com.anggrayudi.storage.file.DocumentFileCompat.fromFullPath
import com.anggrayudi.storage.file.DocumentFileType
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.getAbsolutePath
import com.anggrayudi.storage.file.search
import com.pixnpunk.utils.FileUtil.isWritableDir
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.FileNotFoundException
import kotlin.contracts.ExperimentalContracts

object DirUtil : KoinComponent {
    @OptIn(ExperimentalContracts::class)
    suspend fun receiveGameFilesFromDir(rootDirPath: String): List<String> {
        val gameDir = fromFullPath(
            context = get(),
            fullPath = rootDirPath,
            requiresWriteAccess = true
        )
        if (!gameDir.isWritableDir()) return listOf()

        val files = gameDir.listFiles()
        if (files.size == 0) return listOf()

        val gameFilesPath = mutableListOf<String>()
        for (file in files) {
            val fileName = file.name ?: continue
            if (fileName.endsWith(suffix = "qsp", ignoreCase = true)
                || fileName.endsWith(suffix = "gam", ignoreCase = true)
            ) {
                gameFilesPath.add(file.getAbsolutePath(get()))
            }
        }

        if (gameFilesPath.isEmpty()) {
            gameDir.search(
                recursive = true,
                documentType = DocumentFileType.FILE,
                mimeTypes = arrayOf(MimeType.BINARY_FILE)
            ).collect {
                for (file in it) {
                    val fileName = file.name ?: continue
                    if (fileName.endsWith(suffix = ".qsp", ignoreCase = true)
                        || fileName.endsWith(suffix = ".gam", ignoreCase = true)
                    ) {
                        gameFilesPath.add(file.getAbsolutePath(get()))
                    }
                }
            }
        }

        return gameFilesPath
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun tryReceiveDirSize(rootDirPath: String): Long {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val dir = fromFullPath(get(), rootDirPath)
            if (dir.isWritableDir()) {
                try {
                    val dirMetadata = getDocumentMetadata(get<Context>().contentResolver, dir.uri)
                    if (dirMetadata != null && dirMetadata.containsKey(DocumentsContract.METADATA_TREE_SIZE)) {
                        return dirMetadata.getLong(DocumentsContract.METADATA_TREE_SIZE, 0)
                    }
                } catch (_: FileNotFoundException) {
                }
            }
        }
        return 0
    }
}