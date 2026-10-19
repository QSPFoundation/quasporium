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
import com.pixnpunk.ui.ViewUtil.toComposeFamily
import com.pixnpunk.webview.presentation.RealWebViewComponent
import com.pixnpunk.webview.presentation.WebViewComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

    private data class CachedSettings(
        val raw: SettingsEntity,
        val fontFamily: FontFamily
    )

    private val cachedSettingsFlow: Flow<CachedSettings> = settingsState
        .map { settings ->
            CachedSettings(
                raw = settings,
                fontFamily = resolveFontFamily(settings)
            )
        }
        .flowOn(Dispatchers.IO)
        .distinctUntilChanged()

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
        cachedSettingsFlow
    ) { gameState, processed ->
        if (gameState !is GameState.Running) return@combine MainUIState()

        val settings = processed.raw
        val libConfig = gameState.uiConfig

        MainUIState(
            isActionsVis = gameState.activeWindows.contains(WindowType.ACTS),
            visActsCount = settings.visActsCount,
            parsedActions = gameState.actions.map { item ->
                ParsedActionItem(
                    original = item,
                    annotatedText = AnnotatedString.fromHtml(item.title)
                )
            },
            textColor =
                libConfig.fontColor
                    .takeIf { !settings.isTextColorOverride && it != 0 }
                    ?: settings.textColor,
            backColor =
                libConfig.backColor
                    .takeIf { !settings.isBackColorOverride && it != 0 }
                    ?: settings.backColor,
            fontSize =
                libConfig.fontSize
                    .takeIf { settings.isUseCustomFont && it != 0 }
                    ?: settings.fontSize,
            fontFamily = processed.fontFamily
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
