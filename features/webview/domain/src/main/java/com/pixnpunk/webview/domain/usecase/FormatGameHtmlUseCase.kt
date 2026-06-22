package com.pixnpunk.webview.domain.usecase

import com.pixnpunk.webview.domain.model.HtmlConfig
import com.pixnpunk.webview.domain.repository.HtmlProcessorRepository

class FormatGameHtmlUseCase(
    private val repository: HtmlProcessorRepository
) {
    suspend operator fun invoke(rawBody: String, config: HtmlConfig): String =
        repository.formatGameHtml(rawBody, config)
}