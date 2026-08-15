package com.pixnpunk.webview.presentation

import android.content.Context
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.MimeType
import com.pixnpunk.utils.FileUtil.isWritableFile
import com.pixnpunk.utils.PathUtil.getImageUriFromPath
import com.pixnpunk.utils.PathUtil.normalizeContentPath
import java.io.FileNotFoundException
import kotlin.contracts.ExperimentalContracts

internal class ExternalStoragePathHandler(
    private val context: Context,
    private val gameDirUriBox: String
) : WebViewAssetLoader.PathHandler {
    @OptIn(ExperimentalContracts::class)
    override fun handle(path: String): WebResourceResponse? =
        runCatching {
            val mediaFile = path
                .normalizeContentPath()
                .getImageUriFromPath(
                    context = context,
                    rootDirUriBox = gameDirUriBox,
                ).toDocumentFile(context)
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
