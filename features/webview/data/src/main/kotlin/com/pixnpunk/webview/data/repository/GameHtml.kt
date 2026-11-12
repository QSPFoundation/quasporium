package com.pixnpunk.webview.data.repository

import com.pixnpunk.common.escapeHtml
import org.jsoup.nodes.Document

internal const val PLAIN_TEXT_CLASS = "qsp-plain"

internal fun String.plainTextBody(): String = escapeHtml()

internal fun Document.forbidPageScripts() {
    head().prependElement("meta")
        .attr("http-equiv", "Content-Security-Policy")
        .attr("content", "script-src 'none'")
}
