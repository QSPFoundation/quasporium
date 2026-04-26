package com.pixnpunk.main.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.data.GameNativeEngine
import com.pixnpunk.main.presentation.mvi.MainStore
import com.pixnpunk.main.presentation.mvi.RealMainStore
import com.pixnpunk.webview.GameWebViewComponent
import com.pixnpunk.webview.RealGameWebViewComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent

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

    override val gameWebViewComponent: GameWebViewComponent =
        RealGameWebViewComponent(
            componentContext = childContext("MainGameWebViewComponent"),
            htmlContentFlow = gameNativeEngine.gameStateFlow.map { it.mainDesc }.distinctUntilChanged(),
            settingsStateFlow = settingsState,
            gameDirUriBox = gameDirUriBox,
            onExecValue = onExecValue,
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<MainStore.State> = store.stateFlow
    override val onActionClicked: (Int) -> Unit = { index ->
        store.accept(MainStore.Intent.ActionClick(index))
    }
}
