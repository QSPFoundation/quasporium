package com.pixnpunk.webview.mvi

import com.arkivanov.mvikotlin.core.store.Reducer

object GameWebViewReducer : Reducer<GameWebViewStore.State, GameWebViewStore.Message> {
    override fun GameWebViewStore.State.reduce(msg: GameWebViewStore.Message): GameWebViewStore.State {
        return when (msg) {
            is GameWebViewStore.Message.UpdateHtmlConfig -> copy(htmlConfig = msg.htmlConfig)
            is GameWebViewStore.Message.UpdateHtmlContentHash -> copy(htmlContentHash = msg.htmlContentHash)
            is GameWebViewStore.Message.UpdateScrollYpos -> copy(scrollYpos = msg.scrollYpos)
            is GameWebViewStore.Message.UpdateHtmlContent -> copy(htmlContent = msg.htmlContent)
        }
    }
}