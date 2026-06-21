package com.pixnpunk.webview.mvi

import androidx.compose.runtime.Immutable
import com.arkivanov.mvikotlin.core.store.Store
import com.pixnpunk.webview.model.HtmlConfig
import kotlinx.serialization.Serializable

sealed interface GameWebViewStore : Store<GameWebViewStore.Intent, GameWebViewStore.State, GameWebViewStore.Label> {
    @Immutable
    @Serializable
    data class State(
        val htmlContentHash: Int = 0,
        val scrollYpos: Int = 0,
        val htmlContent: String = "",
        val htmlConfig: HtmlConfig = HtmlConfig()
    )

    sealed interface Intent

    sealed interface Message {
        data class UpdateHtmlConfig(val htmlConfig: HtmlConfig) : Message
        data class UpdateHtmlContentHash(val htmlContentHash: Int) : Message
        data class UpdateScrollYpos(val scrollYpos: Int) : Message
        data class UpdateHtmlContent(val htmlContent: String) : Message
    }

    sealed interface Label

    sealed interface Action {
        data object StartSettingsConv : Action
        data object StartHtmlParsingFlow : Action
    }
}