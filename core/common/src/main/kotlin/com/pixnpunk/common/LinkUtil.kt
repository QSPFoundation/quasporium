package com.pixnpunk.common

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

private const val MAX_SHOWN_URL_LENGTH = 200

private val HttpUrl.displayHost: String
    get() = if (':' in host) "[$host]" else host

fun String.escapeHtml(): String =
    replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")

fun String.makeLinkHost(): String? =
    this.toHttpUrlOrNull()?.displayHost

fun String.makeLinkPrompt(): String {
    val host = this.makeLinkHost()
    val editUrl =
        if (this.length > MAX_SHOWN_URL_LENGTH)
            this.take(MAX_SHOWN_URL_LENGTH) + "…"
        else this

    return buildString {
        append("Open a link from the game?")
        if (host != null) append("<br><br><b>").append(host.escapeHtml()).append("</b>")
        append("<br><br>").append(editUrl.escapeHtml())
    }
}
