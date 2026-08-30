package com.pixnpunk.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.DocumentsContract.getDocumentMetadata
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import com.anggrayudi.storage.file.DocumentFileType
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.StorageId
import com.anggrayudi.storage.file.findParent
import com.anggrayudi.storage.file.getStorageId
import com.anggrayudi.storage.file.search
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.utils.FileUtil.isWritableDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.coroutines.cancellation.CancellationException

object DirUtil : KoinComponent {

    @OptIn(ExperimentalContracts::class)
    fun isGameDirIsInternal(rootDirPath: String): Boolean {
        val gameDir = fromUri(get(), rootDirPath.toUri())
        if (!gameDir.isWritableDir()) return false

        return gameDir.getStorageId(get()) == StorageId.DATA
    }

    @OptIn(ExperimentalContracts::class)
    fun tryReceiveDirSize(rootDirPath: String): Long {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val dirMetadata =
                    getDocumentMetadata(get<Context>().contentResolver, rootDirPath.toUri())
                if (dirMetadata != null && dirMetadata.containsKey(DocumentsContract.METADATA_TREE_SIZE)) {
                    return dirMetadata.getLong(DocumentsContract.METADATA_TREE_SIZE, 0)
                }
            }
        }
        return 0
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun receiveGameDirFromFile(rootDirPath: String): DocumentFile? {
        val gameDir = fromUri(get(), rootDirPath.toUri())
        if (!gameDir.isWritableDir()) return null

        val targetFile = gameDir.search(
            recursive = true,
            documentType = DocumentFileType.FILE,
            mimeTypes = arrayOf(MimeType.BINARY_FILE),
            regex = Regex(".*\\.(qsp|gam)$", RegexOption.IGNORE_CASE)
        ).mapNotNull { it.firstOrNull() }.first()

        return targetFile.findParent(get())
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun receiveGameFilesFromDir(rootDirPath: String): List<String> {
        val gameDir = fromUri(get(), rootDirPath.toUri())
        if (!gameDir.isWritableDir()) return listOf()

        return get<Context>().findGameFiles(
            rootTreeUri = gameDir.uri,
            allowedMimeTypes = setOf(MimeType.BINARY_FILE),
            nameRegex = Regex(".*\\.(qsp|gam)$", RegexOption.IGNORE_CASE)
        ).toList(mutableListOf())
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun receiveGameSavesFromDir(rootDirPath: String): List<String> {
        val gameDir = fromUri(get(), rootDirPath.toUri())
        if (!gameDir.isWritableDir()) return listOf()

        return get<Context>().findGameFiles(
            rootTreeUri = gameDir.uri,
            allowedMimeTypes = setOf(MimeType.BINARY_FILE),
            nameRegex = Regex(".*\\.(sav)$", RegexOption.IGNORE_CASE)
        ).toList(mutableListOf())
    }

    fun Context.findGameFiles(
        rootTreeUri: Uri,
        allowedMimeTypes: Set<String> = emptySet(),
        nameRegex: Regex? = null
    ): Flow<String> = flow {
        when (rootTreeUri.scheme) {
            ContentResolver.SCHEME_FILE -> findFilesWithNio(rootTreeUri, nameRegex)

            ContentResolver.SCHEME_CONTENT ->
                findFilesWithSaf(
                    this@findGameFiles,
                    rootTreeUri,
                    allowedMimeTypes,
                    nameRegex
                )

            else ->
                get<AppLogger>().recordBreadcrumb(
                    "Unsupported URI scheme: ${rootTreeUri.scheme}"
                )
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun FlowCollector<String>.findFilesWithNio(
        rootUri: Uri,
        nameRegex: Regex?
    ) {
        val pathStr = rootUri.path ?: return
        val rootFile = File(pathStr)

        if (!rootFile.exists() || !rootFile.isDirectory) return

        try {
            rootFile.walkTopDown()
                .filter { it.isFile }
                .filter { nameRegex == null || nameRegex.matches(it.name) }
                .forEach { file ->
                    currentCoroutineContext().ensureActive()
                    emit(file.toURI().toString())
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            get<AppLogger>().apply {
                recordBreadcrumb("File IO: Error walking directory tree")
                recordNonFatalException(e)
            }
        }
    }

    private suspend fun FlowCollector<String>.findFilesWithSaf(
        context: Context,
        rootTreeUri: Uri,
        allowedMimeTypes: Set<String>,
        nameRegex: Regex?
    ) {
        val rootDocumentId = try {
            DocumentsContract.getTreeDocumentId(rootTreeUri)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            get<AppLogger>().apply {
                recordBreadcrumb("SAF: Invalid tree URI: $rootTreeUri")
                recordNonFatalException(e)
            }
            return
        }

        val dirIdQueue = ArrayDeque<String>()
        dirIdQueue.addLast(rootDocumentId)

        while (dirIdQueue.isNotEmpty()) {
            currentCoroutineContext().ensureActive()

            val currentDirId = dirIdQueue.removeFirst()
            val childrenUri = try {
                DocumentsContract.buildChildDocumentsUriUsingTree(rootTreeUri, currentDirId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                get<AppLogger>().apply {
                    recordBreadcrumb("SAF: Failed to build child URI for $currentDirId")
                    recordNonFatalException(e)
                }
                continue
            }

            try {
                context.contentResolver.query(
                    childrenUri,
                    arrayOf(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE
                    ),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val idIndex =
                        cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameIndex =
                        cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeIndex =
                        cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)

                    while (cursor.moveToNext()) {
                        currentCoroutineContext().ensureActive()

                        val childId = cursor.getString(idIndex)
                        val childMime = cursor.getString(mimeIndex) ?: ""
                        val childName = cursor.getString(nameIndex) ?: ""

                        if (childMime == DocumentsContract.Document.MIME_TYPE_DIR) {
                            dirIdQueue.addLast(childId)
                            continue
                        }

                        if (allowedMimeTypes.isNotEmpty() && childMime !in allowedMimeTypes) {
                            continue
                        }

                        if (nameRegex != null && !nameRegex.matches(childName)) {
                            continue
                        }

                        emit(
                            DocumentsContract.buildDocumentUriUsingTree(
                                rootTreeUri, childId
                            ).toString()
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                get<AppLogger>().apply {
                    recordBreadcrumb("SAF: Error querying children for dir: $currentDirId")
                    recordNonFatalException(e)
                }
            }
        }
    }
}
