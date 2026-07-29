package com.pixnpunk.extra.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.domain.model.GameState
import com.pixnpunk.game.domain.model.LibUIConfig
import com.pixnpunk.game.domain.repository.GameEngineInteractor
import com.pixnpunk.webview.presentation.RealWebViewComponent
import com.pixnpunk.webview.presentation.WebViewComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent

class RealExtraComponent(
    private val componentContext: ComponentContext,
    private val engineInteractor: GameEngineInteractor,
    private val settingsState: StateFlow<SettingsEntity>,
    private val gameDirUriBox: String,
    private val onExecValue: (String) -> Unit
) : ComponentContext by componentContext, ExtraComponent, KoinComponent {

    override val gameWebViewComponent: WebViewComponent =
        RealWebViewComponent(
            componentContext = childContext("ExtraGameWebViewComponent"),
            htmlContentFlow =
                engineInteractor.state.map { state ->
                    if (state !is GameState.Running) return@map ""
                    state.mainDescription
                }.flowOn(Dispatchers.Default).distinctUntilChanged(),
            engineSettingsStateFlow =
                engineInteractor.state.map { state ->
                    if (state !is GameState.Running) return@map LibUIConfig()
                    state.uiConfig
                }.flowOn(Dispatchers.Default).distinctUntilChanged(),
            settingsStateFlow = settingsState,
            gameDirUriBox = gameDirUriBox,
            onExecValue = onExecValue,
        )
}
