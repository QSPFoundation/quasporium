package com.pixnpunk.webview.data.repository

import com.pixnpunk.common.Base64Util.encodeBase64
import com.pixnpunk.common.escapeHtml
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

internal const val PLAIN_TEXT_CLASS = "qsp-plain"
internal const val IMAGE_LINK_CLASS = "qsp-image"
internal const val IMAGE_LINK_SCHEME = "qspimg"

internal fun String.plainTextBody(): String = escapeHtml()

internal fun Document.forbidPageScripts() {
    head().prependElement("meta")
        .attr("http-equiv", "Content-Security-Policy")
        .attr("content", "script-src 'none'")
}

internal fun Element.linkImagesToFullscreen() =
    select("img").forEach { img ->
        img.attr("src").let {
            if (it.isBlank()
                || img.hasAttr("onclick")
                || img.closest("a") != null
            ) return@forEach

            img.wrap(
                "<a class=\"$IMAGE_LINK_CLASS\" href=\"$IMAGE_LINK_SCHEME:${it.encodeBase64()}\"></a>"
            )
        }
    }
