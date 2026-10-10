package com.pixnpunk.quasporium.mvi

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealRootStore(
    private val storeFactory: StoreFactory
) : KoinComponent {
    internal fun create(): RootStore = RootImpl()

    private inner class RootImpl : RootStore,
        Store<RootStore.Intent, RootStore.State, RootStore.Label> by storeFactory.create(
            name = "RootStore",
            initialState = RootStore.State(),
            bootstrapper = SimpleBootstrapper(
                RootStore.Action.MigrateIfNeeded
            ),
            executorFactory = {
                RootExecutor(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get()
                )
            },
            reducer = { msg ->
                when (msg) {
                    is RootStore.Message.MigrationComplete -> copy(isMigrationComplete = true)
                }
            }
        )
}
