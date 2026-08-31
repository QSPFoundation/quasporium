package com.pixnpunk.webview.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.domain.model.LibUIConfig
import com.pixnpunk.webview.domain.model.HtmlConfig
import com.pixnpunk.webview.domain.usecase.FormatGameHtmlUseCase
import com.pixnpunk.webview.presentation.model.WebViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealWebViewComponent(
    private val componentContext: ComponentContext,
    private val htmlContentFlow: Flow<String>,
    private val settingsStateFlow: StateFlow<SettingsEntity>,
    private val engineSettingsStateFlow: Flow<LibUIConfig>,
    private val isMainComponent: Boolean = false,
    override val gameDirUriBox: String,
    override val onExecValue: (String) -> Unit
) : ComponentContext by componentContext, WebViewComponent, KoinComponent {

    private val scope = coroutineScope()

    private val formatGameHtmlUseCase: FormatGameHtmlUseCase = get()

    private val _model = MutableStateFlow(WebViewState())
    override val model: StateFlow<WebViewState> = _model.asStateFlow()

    override fun onUserTouched() {
        val current = _model.value
        if (current.isAutoScrollActive || current.restoreScrollY > 0) {
            _model.update { it.copy(isAutoScrollActive = false, restoreScrollY = 0) }
        }
    }

    init {
        val configFlow = combine(
            settingsStateFlow,
            engineSettingsStateFlow
        ) { appSet, libSet ->
            HtmlConfig(
                typeface = appSet.typeface,
                isUseHtml = libSet.isUseHtml,
                isUseCustomFont = appSet.isUseCustomFont,
                customFontPath = appSet.customFontPath,
                isImgLoadDis = appSet.isImgLoadDis,
                isAutoWidthImage = appSet.isAutoWidthImage,
                imageWidth = appSet.imageWidth,
                isAutoHeightImage = appSet.isAutoHeightImage,
                imageHeight = appSet.imageHeight,
                isUseFullscreenImages = appSet.isUseFullscreenImages,
                isVideoMute = appSet.isVideoMute,
                fontSize =
                    libSet.fontSize.takeIf { appSet.isUseCustomFont && it != 0 }
                        ?: appSet.fontSize,
                textColor =
                    libSet.fontColor.takeIf { !appSet.isTextColorOverride && it != 0 }
                        ?: appSet.textColor,
                backColor =
                    libSet.backColor.takeIf { !appSet.isBackColorOverride && it != 0 }
                        ?: appSet.backColor,
                linkColor =
                    libSet.linkColor.takeIf { !appSet.isLinkColorOverride && it != 0 }
                        ?: appSet.linkColor,
                isAutoscroll =
                    if (isMainComponent) libSet.isYesScrollMain
                    else libSet.isYesScrollExtra
            )
        }.distinctUntilChanged()

        scope.launch {
            combine(
                flow = htmlContentFlow,
                flow2 = configFlow
            ) { rawHtml, config ->
                formatGameHtmlUseCase(
                    rawBody = rawHtml,
                    config = config
                ).let { Triple(config, it, it.hashCode()) }
            }.flowOn(Dispatchers.Default)
                .collect { (config, formattedHtml, newHash) ->
                    _model.update { current ->
                        val isNewContent = current.htmlContentHash != newHash

                        current.copy(
                            htmlConfig = config,
                            htmlContent = formattedHtml,
                            htmlContentHash = newHash,
                            isAutoScrollActive =
                                if (isNewContent) config.isAutoscroll
                                else current.isAutoScrollActive,
                            restoreScrollY =
                                if (isNewContent) 0
                                else current.restoreScrollY
                        )
                    }
                }
        }
    }
}
