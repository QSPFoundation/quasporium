package com.pixnpunk.webview.presentation

import java.net.URLDecoder

internal const val GAME_FILES_URL = "https://appassets.androidplatform.net/game_files/"

private val SCHEME = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:")

internal fun gameImagePath(src: String): String? {
    val path = when {
        src.startsWith(GAME_FILES_URL) ->
            URLDecoder.decode(src.removePrefix(GAME_FILES_URL).replace("+", "%2B"), "UTF-8")
        SCHEME.containsMatchIn(src) -> null
        else -> src
    }
    return path?.substringBefore('#')?.substringBefore('?')?.takeIf { it.isNotBlank() }
}
