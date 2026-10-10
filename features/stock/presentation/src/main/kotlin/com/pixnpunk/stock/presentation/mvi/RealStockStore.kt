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
            executorFactory = { StockExecutor(get(), get()) },
            reducer = { message ->
                when (message) {
                    is StockStore.Message.ChangeStateDeleteMode -> copy(isDeleteModeActive = message.newState)

                    is StockStore.Message.SelectionUpdated -> copy(selectedGameIds = message.newIds)

                    is StockStore.Message.ClearSelection -> copy(selectedGameIds = emptySet())

                    is StockStore.Message.ChangeSortOrder -> copy(sortOrder = message.newOrder)

                    is StockStore.Message.ChangeSortBy -> copy(sortBy = message.newSort)

                    is StockStore.Message.ChangeSortLang -> copy(selectedLang = message.newLang)

                    is StockStore.Message.ChangeStateSearch -> copy(isSearchExpanded = message.newState)

                    is StockStore.Message.UpdateSearchQuery -> copy(searchQuery = message.newQuery)

                    is StockStore.Message.UpdateActiveTokens -> copy(activeTokens = message.newTokens)

                    is StockStore.Message.UpdateRecentSearches -> copy(recentSearches = message.newSearches)
                }
            }
        )
}
