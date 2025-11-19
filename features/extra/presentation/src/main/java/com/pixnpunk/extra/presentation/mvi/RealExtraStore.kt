package com.pixnpunk.extra.presentation.mvi

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealExtraStore(
    private val storeFactory: StoreFactory
) : KoinComponent {
    internal fun create(): ExtraStore = ExtraImpl()

    private inner class ExtraImpl : ExtraStore,
        Store<ExtraStore.Intent, ExtraStore.State, ExtraStore.Label> by storeFactory.create(
            name = "ExtraStore",
            initialState = ExtraStore.State(),
            bootstrapper = SimpleBootstrapper(ExtraStore.Action.StartExtraReceiveFlow),
            executorFactory = { ExtraExecutor(get(), get()) },
            reducer = ExtraReducer
        )
}