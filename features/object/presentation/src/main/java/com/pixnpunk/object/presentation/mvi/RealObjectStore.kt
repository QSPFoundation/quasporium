package com.pixnpunk.`object`.presentation.mvi

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealObjectStore(
    private val storeFactory: StoreFactory
) : KoinComponent {
    internal fun create(): ObjectStore = ObjectImpl()

    private inner class ObjectImpl : ObjectStore,
        Store<ObjectStore.Intent, ObjectStore.State, ObjectStore.Label> by storeFactory.create(
            name = "ObjectStore",
            initialState = ObjectStore.State(),
            bootstrapper = SimpleBootstrapper(ObjectStore.Action.StartObjectsFlow),
            executorFactory = { ObjectExecutor(get()) },
            reducer = ObjectReducer
        )
}