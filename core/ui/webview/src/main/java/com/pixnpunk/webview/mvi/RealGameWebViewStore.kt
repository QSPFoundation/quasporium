package com.pixnpunk.webview.mvi

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.pixnpunk.dto.SettingsEntity
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
            initialState = GameWebViewStore.State(),
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