package com.pixnpunk.webview

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.webview.mvi.GameWebViewStore
import com.pixnpunk.webview.mvi.RealGameWebViewStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class RealGameWebViewComponent(
    private val componentContext: ComponentContext,
    private val htmlContentFlow: Flow<String>,
    private val settingsStateFlow: StateFlow<SettingsEntity>,
    override val gameDirUriBox: String,
    override val onExecValue: (String) -> Unit
) : ComponentContext by componentContext, GameWebViewComponent {

    private val store = instanceKeeper.getStore {
        RealGameWebViewStore(
            storeFactory = DefaultStoreFactory(),
            htmlContentFlow = htmlContentFlow,
            settingsStateFlow = settingsStateFlow
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<GameWebViewStore.State> = store.stateFlow
}