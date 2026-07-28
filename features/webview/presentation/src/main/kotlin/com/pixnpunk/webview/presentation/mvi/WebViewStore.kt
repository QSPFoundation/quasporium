package com.pixnpunk.webview.presentation.mvi

import androidx.compose.runtime.Immutable
import com.arkivanov.mvikotlin.core.store.Store
import com.pixnpunk.webview.domain.model.HtmlConfig
import kotlinx.serialization.Serializable

sealed interface WebViewStore :
    Store<WebViewStore.Intent, WebViewStore.State, WebViewStore.Label> {
    @Immutable
    @Serializable
    data class State(
        val htmlConfig: HtmlConfig,
        val htmlContent: String = "",
        val htmlContentHash: Int = 0,
        val isAutoScrollActive: Boolean = false,
        val restoreScrollY: Int = 0
    )

    sealed interface Intent {
        data object OnUserTouchedWebView : Intent
    }

    sealed interface Message {
        data class UpdateContent(
            val html: String,
            val hash: Int,
            val autoScroll: Boolean,
            val restoreScrollY: Int
        ) : Message
        data class UpdateHtmlConfig(val htmlConfig: HtmlConfig) : Message
        data object CancelAutoScroll : Message
    }

    sealed interface Label

    sealed interface Action {
        data object StartSettingsConv : Action
        data object StartHtmlParsingFlow : Action
    }
}
