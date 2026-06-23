package com.pixnpunk.webview.domain.repository

import com.pixnpunk.webview.domain.model.HtmlConfig

interface HtmlProcessorRepository {
    suspend fun formatGameHtml(rawBody: String, config: HtmlConfig): String
}