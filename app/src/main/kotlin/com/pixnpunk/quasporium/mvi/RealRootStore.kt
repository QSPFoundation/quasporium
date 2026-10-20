package com.pixnpunk.quasporium.mvi

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

class RealRootStore(
    private val storeFactory: StoreFactory,
    private val executorFactory: () -> RootExecutor
) {
    internal fun create(): RootStore = RootImpl()

    private inner class RootImpl : RootStore,
        Store<RootStore.Intent, RootStore.State, RootStore.Label> by storeFactory.create(
            name = "RootStore",
            initialState = RootStore.State(),
            bootstrapper = SimpleBootstrapper(
                RootStore.Action.MigrateIfNeeded
            ),
            executorFactory = executorFactory,
            reducer = { msg ->
                when (msg) {
                    is RootStore.Message.MigrationComplete -> copy(isMigrationComplete = true)
                }
            }
        )
}
