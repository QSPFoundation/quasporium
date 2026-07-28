package com.pixnpunk.main.presentation

import android.graphics.Typeface
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.fromHtml
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.data.GameNativeEngine
import com.pixnpunk.main.presentation.model.MainUIState
import com.pixnpunk.main.presentation.model.ParsedActionItem
import com.pixnpunk.main.presentation.mvi.MainStore
import com.pixnpunk.main.presentation.mvi.RealMainStore
import com.pixnpunk.utils.ViewUtil.toComposeFamily
import com.pixnpunk.webview.presentation.RealWebViewComponent
import com.pixnpunk.webview.presentation.WebViewComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import java.io.File

class RealMainComponent(
    private val componentContext: ComponentContext,
    private val gameNativeEngine: GameNativeEngine,
    override val settingsState: StateFlow<SettingsEntity>,
    private val gameDirUriBox: String,
    private val onExecValue: (String) -> Unit
) : ComponentContext by componentContext, MainComponent, KoinComponent {

    private val store = instanceKeeper.getStore {
        RealMainStore(
            storeFactory = DefaultStoreFactory(),
            gameNativeEngine = gameNativeEngine
        ).create()
    }

    override val gameWebViewComponent: WebViewComponent =
        RealWebViewComponent(
            componentContext = childContext("MainGameWebViewComponent"),
            htmlContentFlow =
                gameNativeEngine.gameStateFlow
                    .map { it.mainDesc }
                    .distinctUntilChanged(),
            settingsStateFlow = settingsState,
            engineSettingsStateFlow = gameNativeEngine.gameUIConfFlow,
            gameDirUriBox = gameDirUriBox,
            onExecValue = onExecValue,
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<MainStore.State> = store.stateFlow
    override val onActionClicked: (Int) -> Unit = { index ->
        store.accept(MainStore.Intent.ActionClick(index))
    }

    override val uiState: StateFlow<MainUIState> = model
        .map { state ->
            MainUIState(
                isActionsVis = state.isActionsVis,
                parsedActions = state.actions.map { item ->
                    ParsedActionItem(
                        original = item,
                        annotatedText = AnnotatedString.fromHtml(item.text),
                        hasImage = item.imagePath.isNotBlank()
                    )
                }
            )
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = coroutineScope(),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainUIState()
        )

    override val fontFamilyState: StateFlow<FontFamily> = settingsState
        .map { settings ->
            if (settings.isUseCustomFont && settings.customFontPath.isNotEmpty()) {
                val file = File(settings.customFontPath)
                if (file.exists()) {
                    FontFamily(Typeface.createFromFile(file))
                } else {
                    settings.typeface.toComposeFamily()
                }
            } else {
                settings.typeface.toComposeFamily()
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = coroutineScope(),
            started = SharingStarted.Eagerly,
            initialValue = FontFamily.Default
        )
}
