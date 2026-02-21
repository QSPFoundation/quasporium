package com.pixnpunk.details.presentation.mvi

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.pixnpunk.dto.GameEntry
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealDetailsStore(
    private val storeFactory: StoreFactory,
    private val entry: GameEntry
) : KoinComponent {
    internal fun create(): DetailsStore = DetailsImpl()

    private inner class DetailsImpl : DetailsStore,
        Store<DetailsStore.Intent, DetailsStore.State, DetailsStore.Label> by storeFactory.create(
            name = "DetailsStore",
            initialState = DetailsStore.State(gameEntry = entry),
            bootstrapper = SimpleBootstrapper(
                DetailsStore.Action.StartEntryFlow(entry)
            ),
            executorFactory = { DetailsExecutor(get()) },
            reducer = DetailsReducer
        )
}