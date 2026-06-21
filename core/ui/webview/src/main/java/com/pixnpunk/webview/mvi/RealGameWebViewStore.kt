package com.pixnpunk.webview.mvi

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.webview.model.HtmlConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class RealGameWebViewStore(
    private val storeFactory: StoreFactory,
    private val htmlContentFlow: Flow<String>,
    private val settingsStateFlow: StateFlow<SettingsEntity>
) {
    internal fun create(): GameWebViewStore = GameWebViewImpl()

    private inner class GameWebViewImpl : GameWebViewStore,
        Store<GameWebViewStore.Intent, GameWebViewStore.State, GameWebViewStore.Label> by storeFactory.create(
            name = "GameWebViewStore",
            initialState = GameWebViewStore.State(
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
                GameWebViewStore.Action.StartSettingsConv,
                GameWebViewStore.Action.StartHtmlParsingFlow,
            ),
            executorFactory = {
                GameWebViewExecutor(
                    htmlContentFlow,
                    settingsStateFlow
                )
            },
            reducer = GameWebViewReducer
        )
}