package com.pixnpunk.webview.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.webview.domain.usecase.FormatGameHtmlUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WebViewExecutor(
    private val htmlContentFlow: Flow<String>,
    private val formatGameHtmlUseCase: FormatGameHtmlUseCase,
    private val settingsStateFlow: StateFlow<SettingsEntity>
) : CoroutineExecutor<WebViewStore.Intent, WebViewStore.Action, WebViewStore.State, WebViewStore.Message, WebViewStore.Label>() {

    override fun executeAction(action: WebViewStore.Action) {
        when (action) {
            is WebViewStore.Action.StartSettingsConv -> {
                scope.launch {
                    settingsStateFlow.collect {
                        dispatch(
                            WebViewStore.Message.UpdateHtmlConfig(
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

            is WebViewStore.Action.StartHtmlParsingFlow -> {
                scope.launch {
                    htmlContentFlow.collect { rawHtml ->
                        val isNewContent = state().htmlContentHash != rawHtml.hashCode()

                        val doAutoscroll = isNewContent && state().htmlConfig.isAutoscroll
                        val targetRestoreY = if (!isNewContent) state().restoreScrollY else 0

                        val processedHtml = formatGameHtmlUseCase(rawHtml, state().htmlConfig)

                        dispatch(
                            WebViewStore.Message.UpdateContent(
                                html = processedHtml,
                                hash = rawHtml.hashCode(),
                                autoScroll = doAutoscroll,
                                restoreScrollY = targetRestoreY
                            )
                        )
                    }
                }
            }
        }
    }

    override fun executeIntent(intent: WebViewStore.Intent) {
        when (intent) {
            is WebViewStore.Intent.OnUserTouchedWebView -> {
                if (state().isAutoScrollActive || state().restoreScrollY > 0) {
                    dispatch(WebViewStore.Message.CancelAutoScroll)
                }
            }
        }
    }
}
