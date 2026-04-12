package com.pixnpunk.`object`.presentation.mvi

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.pixnpunk.game.data.GameNativeEngine
import org.koin.core.component.KoinComponent

class RealObjectStore(
    private val storeFactory: StoreFactory,
    private val gameNativeEngine: GameNativeEngine
) : KoinComponent {
    internal fun create(): ObjectStore = ObjectImpl()

    private inner class ObjectImpl : ObjectStore,
        Store<ObjectStore.Intent, ObjectStore.State, ObjectStore.Label> by storeFactory.create(
            name = "ObjectStore",
            initialState = ObjectStore.State(),
            bootstrapper = SimpleBootstrapper(ObjectStore.Action.StartObjectsFlow),
            executorFactory = { ObjectExecutor(gameNativeEngine) },
            reducer = ObjectReducer
        )
}