package com.pixnpunk.webview.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewTreeObserver
import android.webkit.WebView

@SuppressLint("ViewConstructor")
internal class AutoScrollWebView(
    context: Context,
    private val onPreDraw: AutoScrollWebView.() -> Unit
) : WebView(context) {

    private var isDestroyed = false

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        if (!isDestroyed) onPreDraw()
        true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnPreDrawListener(preDrawListener)
    }

    override fun onDetachedFromWindow() {
        viewTreeObserver.removeOnPreDrawListener(preDrawListener)
        super.onDetachedFromWindow()
    }

    override fun destroy() {
        isDestroyed = true
        super.destroy()
    }
}
