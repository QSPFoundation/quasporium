package com.pixnpunk.utils

import android.content.Context
import android.os.Build
import android.provider.DocumentsContract
import android.provider.DocumentsContract.getDocumentMetadata
import androidx.core.net.toUri
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import com.anggrayudi.storage.file.DocumentFileType
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.search
import com.pixnpunk.utils.FileUtil.isWritableDir
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.contracts.ExperimentalContracts

object DirUtil : KoinComponent {
    @OptIn(ExperimentalContracts::class)
    suspend fun receiveGameFilesFromDir(rootDirPath: String): List<String> {
        val gameDir = fromUri(get(), rootDirPath.toUri())
        if (!gameDir.isWritableDir()) return listOf()

        val files = gameDir.listFiles()
        if (files.size <= 0) return listOf()

        val gameFilesPath = mutableListOf<String>()
        for (file in files) {
            val fileName = file.name ?: continue
            if (fileName.endsWith(suffix = "qsp", ignoreCase = true)
                || fileName.endsWith(suffix = "gam", ignoreCase = true)
            ) {
                gameFilesPath.add(file.uri.toString())
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
                        gameFilesPath.add(file.uri.toString())
                    }
                }
            }
        }

        return gameFilesPath.distinct()
    }

    @OptIn(ExperimentalContracts::class)
    fun tryReceiveDirSize(rootDirPath: String): Long {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val dirMetadata = getDocumentMetadata(get<Context>().contentResolver, rootDirPath.toUri())
                if (dirMetadata != null && dirMetadata.containsKey(DocumentsContract.METADATA_TREE_SIZE)) {
                    return dirMetadata.getLong(DocumentsContract.METADATA_TREE_SIZE, 0)
                }
            }
        }
        return 0
    }
}