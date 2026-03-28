package com.pixnpunk.game.presentation

import android.content.Context
import android.webkit.WebResourceResponse
import androidx.core.net.toUri
import androidx.webkit.WebViewAssetLoader
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.child
import com.pixnpunk.utils.FileUtil.isWritableDir
import com.pixnpunk.utils.FileUtil.isWritableFile
import java.io.FileNotFoundException
import kotlin.contracts.ExperimentalContracts

internal class ExternalStoragePathHandler(
    private val context: Context,
    private val gameDirUriBox: String
) : WebViewAssetLoader.PathHandler {
    @OptIn(ExperimentalContracts::class)
    override fun handle(path: String): WebResourceResponse? {
        val mRootDir = gameDirUriBox.toUri().toDocumentFile(context)
        if (!mRootDir.isWritableDir()) return null
        return runCatching {
            val mediaFile = mRootDir.child(context, path)
            if (!mediaFile.isWritableFile()) {
                throw FileNotFoundException()
            } else {
                WebResourceResponse(
                    MimeType.getMimeTypeFromFileName(mediaFile.name),
                    null,
                    200,
                    "OK",
                    mapOf(
                        "Cache-Control" to "public, max-age=3600, immutable",
                        "Access-Control-Allow-Origin" to "*"
                    ),
                    context.contentResolver.openInputStream(mediaFile.uri)
                )
            }
        }.getOrElse { null }
    }
}