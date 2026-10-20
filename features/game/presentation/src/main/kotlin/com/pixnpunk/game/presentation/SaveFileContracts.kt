package com.pixnpunk.game.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.anggrayudi.storage.file.MimeType.BINARY_FILE

internal data class SaveFileRequest(val fileName: String, val initialUri: Uri?)

internal class CreateSaveFileContract : ActivityResultContract<SaveFileRequest, Uri?>() {
    private val createDocument = ActivityResultContracts.CreateDocument(BINARY_FILE)

    override fun createIntent(context: Context, input: SaveFileRequest): Intent =
        createDocument.createIntent(context, input.fileName).withInitialUri(input.initialUri)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        createDocument.parseResult(resultCode, intent)
}

internal class OpenSaveFileContract : ActivityResultContract<Uri?, Uri?>() {
    private val openDocument = ActivityResultContracts.OpenDocument()

    override fun createIntent(context: Context, input: Uri?): Intent =
        openDocument.createIntent(context, arrayOf(BINARY_FILE)).withInitialUri(input)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        openDocument.parseResult(resultCode, intent)
}

internal fun initialFolderUri(treeUri: String): Uri? =
    runCatching {
        val tree = treeUri.toUri()
        DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
    }.getOrNull()

private fun Intent.withInitialUri(uri: Uri?): Intent =
    apply { if (uri != null) putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri) }
