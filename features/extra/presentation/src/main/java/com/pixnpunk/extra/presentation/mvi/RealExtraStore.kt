package com.pixnpunk.extra.presentation.mvi

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.data.GameNativeEngine
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent

class RealExtraStore(
    private val storeFactory: StoreFactory,
    private val gameNativeEngine: GameNativeEngine,
    private val stateKeeper: StateKeeper
) : KoinComponent {
    internal fun create(): ExtraStore = ExtraImpl()

    private inner class ExtraImpl : ExtraStore,
        Store<ExtraStore.Intent, ExtraStore.State, ExtraStore.Label> by storeFactory.create(
            name = "ExtraStore",
            initialState =
                stateKeeper.consume(
                    key = "ExtraStoreSavedState",
                    strategy = ExtraStore.State.serializer()
                ) ?: ExtraStore.State(),
            bootstrapper = SimpleBootstrapper(
                ExtraStore.Action.StartExtraReceiveFlow
            ),
            executorFactory = { ExtraExecutor(gameNativeEngine) },
            reducer = ExtraReducer
        )
}