package com.pixnpunk.game.presentation

import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import androidx.webkit.WebViewAssetLoader
import com.pixnpunk.utils.Base64Util.decodeBase64
import com.pixnpunk.utils.Base64Util.isBase64
import com.pixnpunk.utils.HtmlUtil.isContainsHtmlTags
import com.pixnpunk.utils.HtmlUtil.removeHtmlTags
import kotlin.contracts.ExperimentalContracts

internal class GameWebViewClient(
    private val assetLoader: WebViewAssetLoader,
    private val onExecValue: (String) -> Unit,
    private val onStartActivity: (Intent) -> Unit
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest
    ): Boolean {
        val uriScheme = request.url.scheme ?: return false
        val uriDecode = Uri.decode(request.url.toString())

        when (uriScheme) {
            "exec" ->
                uriDecode.substring(5)
                    .let {
                        if (it.isBase64()) {
                            it.decodeBase64()
                        } else {
                            it
                        }
                    }
                    .let {
                        if (it.isContainsHtmlTags()) {
                            onExecValue(it.removeHtmlTags())
                        } else {
                            onExecValue(it)
                        }
                    }

            "https", "http" ->
                Intent(Intent.ACTION_VIEW, uriDecode.toUri())
                    .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    .also(onStartActivity)

            "file" ->
                Intent(Intent.ACTION_VIEW, uriScheme.replace("file:/", "https:").toUri())
                    .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    .also(onStartActivity)
        }
        return true
    }

    @OptIn(ExperimentalContracts::class)
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ) = runCatching {
        val uriPath = request.url ?: throw NullPointerException()
        assetLoader.shouldInterceptRequest(uriPath)
    }.getOrElse { super.shouldInterceptRequest(view, request) }
}