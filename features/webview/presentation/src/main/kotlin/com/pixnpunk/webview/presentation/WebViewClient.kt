package com.pixnpunk.webview.presentation

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import com.pixnpunk.common.Base64Util.decodeBase64

@SuppressLint("MissingOnRenderProcessGone")
internal class WebViewClient(
    private val assetLoader: Lazy<WebViewAssetLoader>,
    private val onExecValue: (String) -> Unit,
    private val onOpenLink: (String) -> Unit,
    private val onOpenImage: (String) -> Unit,
    private val onRenderProcessGone: () -> Unit
) : WebViewClient() {

    override fun onRenderProcessGone(
        view: WebView,
        detail: RenderProcessGoneDetail
    ): Boolean {
        onRenderProcessGone()
        return true
    }

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
                if (request.hasGesture()) onOpenLink(request.url.toString())

            "qspimg" ->
                if (request.hasGesture()) {
                    runCatching { request.url.schemeSpecificPart.decodeBase64() }
                        .getOrNull()
                        ?.let(::gameImagePath)
                        ?.let(onOpenImage)
                }
        }
        return true
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ) = runCatching {
        assetLoader.value.shouldInterceptRequest(
            request.url ?: throw NullPointerException()
        )
    }.getOrElse { super.shouldInterceptRequest(view, request) }
}
