package com.pixnpunk.extra.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.data.GameNativeEngine
import com.pixnpunk.webview.presentation.WebViewComponent
import com.pixnpunk.webview.presentation.RealWebViewComponent
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent

class RealExtraComponent(
    private val componentContext: ComponentContext,
    private val gameNativeEngine: GameNativeEngine,
    private val settingsState: StateFlow<SettingsEntity>,
    private val gameDirUriBox: String,
    private val onExecValue: (String) -> Unit
) : ComponentContext by componentContext, ExtraComponent, KoinComponent {

    override val gameWebViewComponent: WebViewComponent =
        RealWebViewComponent(
            componentContext = childContext("ExtraGameWebViewComponent"),
            htmlContentFlow =
                gameNativeEngine.gameStateFlow
                    .map { it.varsDesc }
                    .distinctUntilChanged(),
            settingsStateFlow = settingsState,
            engineSettingsStateFlow = gameNativeEngine.gameUIConfFlow,
            gameDirUriBox = gameDirUriBox,
            onExecValue = onExecValue,
        )
}
