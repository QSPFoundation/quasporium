package com.pixnpunk.webview.presentation.model

import androidx.compose.runtime.Immutable
import com.pixnpunk.webview.domain.model.HtmlConfig
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class WebViewState(
    val htmlConfig: HtmlConfig = HtmlConfig(),
    val htmlContent: String = "",
    val htmlContentHash: Int = 0,
    val isAutoScrollActive: Boolean = false,
    val restoreScrollY: Int = 0
)
