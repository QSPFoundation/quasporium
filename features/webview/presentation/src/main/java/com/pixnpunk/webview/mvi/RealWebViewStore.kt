package com.pixnpunk.webview.mvi

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.webview.domain.model.HtmlConfig
import com.pixnpunk.webview.domain.usecase.FormatGameHtmlUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class RealWebViewStore(
    private val storeFactory: StoreFactory,
    private val htmlContentFlow: Flow<String>,
    private val settingsStateFlow: StateFlow<SettingsEntity>,
    private val formatGameHtmlUseCase: FormatGameHtmlUseCase
) {
    internal fun create(): WebViewStore = WebViewImpl()

    private inner class WebViewImpl : WebViewStore,
        Store<WebViewStore.Intent, WebViewStore.State, WebViewStore.Label> by storeFactory.create(
            name = "GameWebViewStore",
            initialState = WebViewStore.State(
                htmlConfig = HtmlConfig(
                    typeface = settingsStateFlow.value.typeface,
                    fontSize = settingsStateFlow.value.fontSize,
                    textColor = settingsStateFlow.value.textColor,
                    backColor = settingsStateFlow.value.backColor,
                    linkColor = settingsStateFlow.value.linkColor,
                    isUseCustomFont = settingsStateFlow.value.isUseCustomFont,
                    customFontPath = settingsStateFlow.value.customFontPath,
                    isImgLoadDis = settingsStateFlow.value.isImgLoadDis,
                    isAutoWidthImage = settingsStateFlow.value.isAutoWidthImage,
                    imageWidth = settingsStateFlow.value.imageWidth,
                    isAutoHeightImage = settingsStateFlow.value.isAutoHeightImage,
                    imageHeight = settingsStateFlow.value.imageHeight,
                    isUseFullscreenImages = settingsStateFlow.value.isUseFullscreenImages,
                    isVideoMute = settingsStateFlow.value.isVideoMute,
                    isAutoscroll = settingsStateFlow.value.isYesScrollMain,
                    isUseHtml = settingsStateFlow.value.isUseHtml
                )
            ),
            bootstrapper = SimpleBootstrapper(
                WebViewStore.Action.StartSettingsConv,
                WebViewStore.Action.StartHtmlParsingFlow
            ),
            executorFactory = {
                WebViewExecutor(
                    htmlContentFlow = htmlContentFlow,
                    formatGameHtmlUseCase = formatGameHtmlUseCase,
                    settingsStateFlow = settingsStateFlow
                )
            },
            reducer = { msg ->
                when (msg) {
                    is WebViewStore.Message.UpdateContent -> copy(
                        htmlContent = msg.html,
                        htmlContentHash = msg.hash,
                        isAutoScrollActive = msg.autoScroll,
                        restoreScrollY = msg.restoreScrollY
                    )

                    is WebViewStore.Message.UpdateHtmlConfig -> copy(
                        htmlConfig = msg.htmlConfig
                    )

                    is WebViewStore.Message.CancelAutoScroll -> copy(
                        isAutoScrollActive = false,
                        restoreScrollY = 0
                    )
                }
            }
        )
}