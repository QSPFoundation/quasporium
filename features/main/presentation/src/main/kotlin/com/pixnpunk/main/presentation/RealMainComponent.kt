package com.pixnpunk.main.presentation

import android.graphics.Typeface
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.fromHtml
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.domain.model.GameState
import com.pixnpunk.game.domain.model.LibUIConfig
import com.pixnpunk.game.domain.model.WindowType
import com.pixnpunk.game.domain.repository.EngineCommand
import com.pixnpunk.game.domain.repository.GameEngineInteractor
import com.pixnpunk.main.presentation.model.MainUIState
import com.pixnpunk.main.presentation.model.ParsedActionItem
import com.pixnpunk.utils.ViewUtil.toComposeFamily
import com.pixnpunk.webview.presentation.RealWebViewComponent
import com.pixnpunk.webview.presentation.WebViewComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import java.io.File

class RealMainComponent(
    private val componentContext: ComponentContext,
    private val engineInteractor: GameEngineInteractor,
    private val settingsState: StateFlow<SettingsEntity>,
    private val gameDirUriBox: String,
    private val onExecValue: (String) -> Unit
) : ComponentContext by componentContext, MainComponent, KoinComponent {

    override val onActionClicked: (Int) -> Unit = { index ->
        engineInteractor.submitCommand(EngineCommand.ExecuteAction(index))
    }

    override val gameWebViewComponent: WebViewComponent =
        RealWebViewComponent(
            componentContext = childContext("MainGameWebViewComponent"),
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
            isMainComponent = true,
            gameDirUriBox = gameDirUriBox,
            onExecValue = onExecValue,
        )

    override val uiState: StateFlow<MainUIState> = combine(
        engineInteractor.state,
        settingsState
    ) { gameState, settings ->
        if (gameState !is GameState.Running) return@combine MainUIState()
        MainUIState(
            isActionsVis = gameState.activeWindows.contains(WindowType.ACTS),
            parsedActions = gameState.actions.map { item ->
                ParsedActionItem(
                    original = item,
                    annotatedText = AnnotatedString.fromHtml(item.title)
                )
            },
            backColor =
                gameState.uiConfig.backColor
                    .takeIf { !settings.isBackColorOverride && it != 0 }
                    ?: settings.backColor,
            fontSize =
                gameState.uiConfig.fontSize
                    .takeIf { settings.isUseGameFont && it != 0 }
                    ?: settings.fontSize,
            fontFamily = resolveFontFamily(settings)
        )
    }.flowOn(Dispatchers.Default).stateIn(
        scope = coroutineScope(),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUIState()
    )

    private fun resolveFontFamily(
        settings: SettingsEntity
    ): FontFamily {
        if (settings.isUseCustomFont && settings.customFontPath.isNotEmpty()) {
            val file = File(settings.customFontPath)
            if (file.exists()) {
                return runCatching {
                    FontFamily(Typeface.createFromFile(file))
                }.getOrDefault(
                    settings.typeface.toComposeFamily()
                )
            }
        }

        return settings.typeface.toComposeFamily()
    }
}
