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
            bootstrapper = SimpleBootstrapper(DetailsStore.Action.Init(entry)),
            executorFactory = {
                DetailsExecutor(
                    get(),
                    get(),
                    get(),
                    get()
                )
            },
            reducer = { msg ->
                when (msg) {
                    is DetailsStore.Message.SetLoading -> copy(isLoading = msg.isLoading)
                    is DetailsStore.Message.UpdateEntry -> copy(gameEntry = msg.entry)
                    is DetailsStore.Message.UpdateProfiles -> copy(gameProfiles = msg.profiles)
                    is DetailsStore.Message.UpdateGameFiles -> copy(gameFiles = msg.files)
                }
            }
        )
}
