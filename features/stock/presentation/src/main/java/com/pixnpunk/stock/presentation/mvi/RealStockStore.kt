package com.pixnpunk.stock.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealStockStore(
    private val storeFactory: StoreFactory
) : KoinComponent {
    internal fun create(): StockStore = StockImpl()

    private inner class StockImpl : StockStore,
        Store<StockStore.Intent, StockStore.State, StockStore.Label> by storeFactory.create(
            name = "StockStore",
            initialState = StockStore.State(),
            executorFactory = { StockExecutor(get()) },
            reducer = { message ->
                when (message) {
                    is StockStore.Message.ChangeStateDeleteMode -> copy(isDeleteModeActive = message.newState)

                    is StockStore.Message.SelectionUpdated -> copy(selectedGameIds = message.newIds)

                    is StockStore.Message.ClearSelection -> copy(selectedGameIds = emptySet())
                }
            }
        )
}