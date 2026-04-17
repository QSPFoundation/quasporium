package com.pixnpunk.extra.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.extra.presentation.mvi.ExtraStore
import com.pixnpunk.extra.presentation.mvi.RealExtraStore
import com.pixnpunk.game.data.GameNativeEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.serializer
import org.koin.core.component.KoinComponent

class RealExtraComponent(
    private val componentContext: ComponentContext,
    private val gameNativeEngine: GameNativeEngine,
    override val settingsState: StateFlow<SettingsEntity>,
    override val gameDirUriBox: String,
    override val onExecValue: (String) -> Unit
) : ComponentContext by componentContext, ExtraComponent, KoinComponent {

    private val store = instanceKeeper.getStore {
        RealExtraStore(
            storeFactory = DefaultStoreFactory(),
            gameNativeEngine = gameNativeEngine,
            stateKeeper = stateKeeper,
            settingsState = settingsState
        ).create()
    }

    init {
        stateKeeper.register(
            key = "ExtraStoreSavedState",
            strategy = serializer(),
            supplier = { store.state }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<ExtraStore.State> = store.stateFlow
}
