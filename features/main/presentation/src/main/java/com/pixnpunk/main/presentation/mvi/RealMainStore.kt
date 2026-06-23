package com.pixnpunk.main.presentation.mvi

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.pixnpunk.game.data.GameNativeEngine
import org.koin.core.component.KoinComponent

class RealMainStore(
    private val storeFactory: StoreFactory,
    private val gameNativeEngine: GameNativeEngine,
) : KoinComponent {
    internal fun create(): MainStore = MainImpl()

    private inner class MainImpl : MainStore,
        Store<MainStore.Intent, MainStore.State, MainStore.Label> by storeFactory.create(
            name = "MainStore",
            initialState = MainStore.State(),
            bootstrapper = SimpleBootstrapper(
                MainStore.Action.StartContentFlow,
                MainStore.Action.StartActionsVisFlow
            ),
            executorFactory = { MainExecutor(gameNativeEngine) },
            reducer = MainReducer
        )
}