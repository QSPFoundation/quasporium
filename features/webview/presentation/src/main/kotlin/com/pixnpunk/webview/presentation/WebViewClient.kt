package com.pixnpunk.webview.presentation

import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import androidx.webkit.WebViewAssetLoader
import com.pixnpunk.utils.Base64Util.decodeBase64
import kotlin.contracts.ExperimentalContracts

internal class WebViewClient(
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
                        onExecValue(
                            runCatching {
                                it.decodeBase64()
                            }.getOrDefault(it)
                        )
                    }

            "https", "http" ->
                if (request.hasGesture()) {
                    Intent(Intent.ACTION_VIEW, uriDecode.toUri())
                        .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                        .also(onStartActivity)
                }
        }
        return true
    }

    @OptIn(ExperimentalContracts::class)
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ) = runCatching {
        assetLoader.shouldInterceptRequest(
            request.url ?: throw NullPointerException()
        )
    }.getOrElse { super.shouldInterceptRequest(view, request) }
}
