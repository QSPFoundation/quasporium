package com.pixnpunk.webview

import android.content.Context
import android.webkit.WebView

internal class StatefulWebView(context: Context) : WebView(context) {
    var isPageLoaded = false
    var onScrollStateChanged: ((Int) -> Unit)? = null

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (isPageLoaded) {
            onScrollStateChanged?.invoke(t)
        }
    }
}