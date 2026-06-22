package com.pixnpunk.webview.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.webview.HtmlEngine.appendPageTemplate
import com.pixnpunk.webview.HtmlEngine.getCleanHtmlAndMedia
import com.pixnpunk.webview.HtmlEngine.getCleanHtmlRemMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GameWebViewExecutor(
    private val htmlContentFlow: Flow<String>
) : CoroutineExecutor<GameWebViewStore.Intent, GameWebViewStore.Action, GameWebViewStore.State, GameWebViewStore.Message, GameWebViewStore.Label>() {

    override fun executeAction(action: GameWebViewStore.Action) {
        when (action) {
            is GameWebViewStore.Action.StartHtmlParsingFlow -> {
                scope.launch(Dispatchers.Default) {
                    htmlContentFlow.collect {
                        val isNewContent = state().htmlContentHash != it.hashCode()
                        val targetRestoreY = if (!isNewContent) state().scrollYpos else 0
                        val doAutoscroll = isNewContent && state().htmlConfig.isAutoscroll

                        val htmlContent = appendPageTemplate(
                            config = state().htmlConfig,
                            body = it,
                            autoscroll = doAutoscroll,
                            restoreScrollY = targetRestoreY
                        ).let { s ->
                            if (state().htmlConfig.isImgLoadDis) {
                                s.getCleanHtmlRemMedia()
                            } else {
                                s.getCleanHtmlAndMedia(state().htmlConfig)
                            }
                        }

                        scope.launch {
                            if (isNewContent) {
                                dispatch(GameWebViewStore.Message.UpdateScrollYpos(0))
                                dispatch(GameWebViewStore.Message.UpdateHtmlContentHash(it.hashCode()))
                            }
                            dispatch(GameWebViewStore.Message.UpdateHtmlContent(htmlContent))
                        }
                    }
                }
            }
        }
    }
}