package com.pixnpunk.stock.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.stock.domain.model.SearchToken
import com.pixnpunk.stock.domain.model.SearchTokenType
import com.pixnpunk.stock.domain.model.SortOrder
import com.pixnpunk.stock.domain.usecase.DeleteLocalGameEntriesUseCase
import com.pixnpunk.stock.presentation.mvi.StockStore.Message.ChangeStateDeleteMode
import com.pixnpunk.stock.presentation.mvi.StockStore.Message.ClearSelection
import com.pixnpunk.stock.presentation.mvi.StockStore.Message.SelectionUpdated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StockExecutor(
    private val deleteLocalGameEntriesUseCase: DeleteLocalGameEntriesUseCase
) : CoroutineExecutor<StockStore.Intent, StockStore.Action, StockStore.State, StockStore.Message, StockStore.Label>() {

    override fun executeIntent(intent: StockStore.Intent) {
        when (intent) {
            is StockStore.Intent.EnterDeleteMode -> dispatch(ChangeStateDeleteMode(true))

            is StockStore.Intent.ExitDeleteMode -> {
                dispatch(ClearSelection)
                dispatch(ChangeStateDeleteMode(false))
            }

            is StockStore.Intent.AddAllVisibleGames -> dispatch(SelectionUpdated(intent.gameIds))

            is StockStore.Intent.DeleteSelectedGames -> {
                val idsToDelete = state().selectedGameIds
                if (idsToDelete.isNotEmpty()) {
                    scope.launch(Dispatchers.IO) {
                        deleteLocalGameEntriesUseCase.execute(idsToDelete)
                        withContext(Dispatchers.Main) {
                            dispatch(ClearSelection)
                            dispatch(ChangeStateDeleteMode(false))
                        }
                    }
                }
            }

            is StockStore.Intent.ToggleGameSelection -> {
                val currentIds = state().selectedGameIds
                val id = intent.gameId
                val newIds = if (currentIds.contains(id)) currentIds - id else currentIds + id
                dispatch(SelectionUpdated(newIds))
            }

            is StockStore.Intent.ToggleSortOrder ->
                dispatch(
                    StockStore.Message.ChangeSortOrder(
                        if (state().sortOrder == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING
                    )
                )

            is StockStore.Intent.SelectSort ->
                dispatch(StockStore.Message.ChangeSortBy(intent.sortBy))

            is StockStore.Intent.SelectLanguage ->
                dispatch(StockStore.Message.ChangeSortLang(intent.lang))

            is StockStore.Intent.ToggleSearch ->
                dispatch(StockStore.Message.ChangeStateSearch(intent.isExpanded))

            is StockStore.Intent.UpdateQuery ->
                dispatch(StockStore.Message.UpdateSearchQuery(intent.query))

            is StockStore.Intent.ExecuteSearch -> {
                if (state().searchQuery.length >= 3) {
                    val newRecent = state().recentSearches + SearchToken(
                        state().searchQuery,
                        SearchTokenType.TITLE,
                        state().searchQuery
                    )
                    dispatch(
                        StockStore.Message.UpdateRecentSearches(
                            newRecent.toSet().toList()
                        )
                    )
                }
                dispatch(StockStore.Message.ChangeStateSearch(false))
            }

            is StockStore.Intent.AddToken -> {
                dispatch(StockStore.Message.UpdateSearchQuery(""))
                dispatch(StockStore.Message.UpdateRecentSearches(state().recentSearches + intent.token))
                dispatch(StockStore.Message.UpdateActiveTokens(state().activeTokens + intent.token))
                dispatch(StockStore.Message.ChangeStateSearch(false))
            }

            is StockStore.Intent.RemoveToken -> {
                dispatch(StockStore.Message.UpdateActiveTokens(state().activeTokens - intent.token))
            }
        }
    }
}
