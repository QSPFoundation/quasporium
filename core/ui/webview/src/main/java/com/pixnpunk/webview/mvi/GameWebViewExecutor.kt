package com.pixnpunk.webview.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.webview.HtmlEngine.appendPageTemplate
import com.pixnpunk.webview.HtmlEngine.getCleanHtmlAndMedia
import com.pixnpunk.webview.HtmlEngine.getCleanHtmlRemMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameWebViewExecutor(
    private val htmlContentFlow: Flow<String>,
    private val settingsStateFlow: StateFlow<SettingsEntity>
) : CoroutineExecutor<GameWebViewStore.Intent, GameWebViewStore.Action, GameWebViewStore.State, GameWebViewStore.Message, GameWebViewStore.Label>() {

    override fun executeAction(action: GameWebViewStore.Action) {
        when (action) {
            is GameWebViewStore.Action.StartSettingsConv -> {
                scope.launch(Dispatchers.Default) {
                    settingsStateFlow.collect {
                        scope.launch {
                            dispatch(
                                GameWebViewStore.Message.UpdateHtmlConfig(
                                    state().htmlConfig.copy(
                                        typeface = it.typeface,
                                        fontSize = it.fontSize,
                                        textColor = it.textColor,
                                        backColor = it.backColor,
                                        linkColor = it.linkColor,
                                        isUseCustomFont = it.isUseCustomFont,
                                        customFontPath = it.customFontPath,
                                        isImgLoadDis = it.isImgLoadDis,
                                        isAutoWidthImage = it.isAutoWidthImage,
                                        imageWidth = it.imageWidth,
                                        isAutoHeightImage = it.isAutoHeightImage,
                                        imageHeight = it.imageHeight,
                                        isUseFullscreenImages = it.isUseFullscreenImages,
                                        isVideoMute = it.isVideoMute,
                                        isAutoscroll = it.isYesScrollMain,
                                        isUseHtml = it.isUseHtml
                                    )
                                )
                            )
                        }
                    }
                }
            }

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