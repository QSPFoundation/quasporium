package com.pixnpunk.storage

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
import com.pixnpunk.storage.FileUtil.isWritableDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
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
import java.io.IOException
import kotlin.contracts.ExperimentalContracts
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

object DirUtil : KoinComponent {

    @OptIn(ExperimentalContracts::class)
    fun isGameDirIsInternal(rootDirPath: String): Boolean =
        fromUri(get(), rootDirPath.toUri()).run {
            isWritableDir() && getStorageId(get()) == StorageId.DATA
        }

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
    suspend fun receiveGameSavesFromDir(rootDirPath: String): List<String>? {
        val gameDir = fromUri(get(), rootDirPath.toUri())
        if (!gameDir.isWritableDir()) return null

        return try {
            get<Context>().findGameFiles(
                rootTreeUri = gameDir.uri,
                allowedMimeTypes = setOf(MimeType.BINARY_FILE),
                nameRegex = Regex(".*\\.(sav)$", RegexOption.IGNORE_CASE),
                failOnError = true
            ).toList(mutableListOf())
        } catch (_: IOException) { null }
    }

    fun Context.findGameFiles(
        rootTreeUri: Uri,
        allowedMimeTypes: Set<String> = emptySet(),
        nameRegex: Regex? = null,
        failOnError: Boolean = false
    ): Flow<String> = flow {
        when (rootTreeUri.scheme) {
            ContentResolver.SCHEME_FILE ->
                findFilesWithNio(
                    rootTreeUri,
                    nameRegex,
                    failOnError
                )

            ContentResolver.SCHEME_CONTENT ->
                findFilesWithSaf(
                    this@findGameFiles,
                    rootTreeUri,
                    allowedMimeTypes,
                    nameRegex,
                    failOnError
                )

            else -> {
                get<AppLogger>().recordBreadcrumb(
                    "Unsupported URI scheme: ${rootTreeUri.scheme}"
                )
                if (failOnError) throw IOException("Unsupported URI scheme: ${rootTreeUri.scheme}")
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun FlowCollector<String>.findFilesWithNio(
        rootUri: Uri,
        nameRegex: Regex?,
        failOnError: Boolean
    ) {
        val pathStr = rootUri.path ?: return
        val rootFile = File(pathStr)

        if (!rootFile.exists() || !rootFile.isDirectory) {
            if (failOnError) throw IOException("Not a directory")
            return
        }

        try {
            rootFile.walkTopDown()
                .onFail { _, e -> if (failOnError) throw e }
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
            if (failOnError) throw IOException("Error walking directory tree", e)
        }
    }

    private suspend fun FlowCollector<String>.findFilesWithSaf(
        context: Context,
        rootTreeUri: Uri,
        allowedMimeTypes: Set<String>,
        nameRegex: Regex?,
        failOnError: Boolean,
        ignoredDirNames: Set<String> = setOf(
            "images", "img", "sounds", "sound", "audio", "video", "music"
        )
    ) {
        val rootDocumentId = try {
            DocumentsContract.getTreeDocumentId(rootTreeUri)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            get<AppLogger>().apply {
                recordBreadcrumb("SAF: Invalid tree URI")
                recordNonFatalException(e)
            }
            if (failOnError) throw IOException("Invalid tree URI", e)
            return
        }

        val dirIdQueue = ArrayDeque<String>()
        dirIdQueue.addLast(rootDocumentId)

        var operationsCount = 0

        while (dirIdQueue.isNotEmpty()) {
            currentCoroutineContext().ensureActive()

            if (++operationsCount % 30 == 0) delay(10.milliseconds)

            val currentDirId = dirIdQueue.removeFirst()
            val childrenUri = try {
                DocumentsContract.buildChildDocumentsUriUsingTree(
                    rootTreeUri,
                    currentDirId
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                get<AppLogger>().apply {
                    recordBreadcrumb("SAF: Failed to build child URI")
                    recordNonFatalException(e)
                }
                if (failOnError) throw IOException("Failed to build child URI", e)
                continue
            }

            var attempts = 0
            var success = false

            while (attempts < 3 && !success) {
                attempts++
                val childDirIds = mutableListOf<String>()
                val childFileUris = mutableListOf<String>()
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
                                if (
                                    childName.lowercase() in ignoredDirNames
                                    && nameRegex?.pattern?.contains(childName) != true
                                ) {
                                    continue
                                }
                                childDirIds.add(childId)
                                continue
                            }

                            if (allowedMimeTypes.isNotEmpty() && childMime !in allowedMimeTypes) {
                                continue
                            }

                            if (nameRegex != null && !nameRegex.matches(childName)) {
                                continue
                            }

                            childFileUris.add(
                                DocumentsContract.buildDocumentUriUsingTree(
                                    rootTreeUri, childId
                                ).toString()
                            )
                        }
                    } ?: throw IOException("Provider returned no cursor")
                    success = true
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    if (attempts < 3) {
                        delay((100L * attempts).milliseconds)
                    } else {
                        get<AppLogger>().apply {
                            recordBreadcrumb("SAF: Error querying children for dir after retries")
                            recordNonFatalException(e)
                        }
                        if (failOnError) throw IOException("Error querying children", e)
                    }
                }
                if (success) {
                    childDirIds.forEach(dirIdQueue::addLast)
                    childFileUris.forEach { emit(it) }
                }
            }
        }
    }
}
