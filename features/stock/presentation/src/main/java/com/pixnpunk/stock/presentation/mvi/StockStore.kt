package com.pixnpunk.stock.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Store
import com.pixnpunk.stock.domain.model.SearchToken
import com.pixnpunk.stock.domain.model.SortBy
import com.pixnpunk.stock.domain.model.SortOrder
import kotlinx.serialization.Serializable

sealed interface StockStore : Store<StockStore.Intent, StockStore.State, StockStore.Label> {
    @Serializable
    data class State(
        val isDeleteModeActive: Boolean = false,
        val selectedGameIds: Set<Long> = emptySet(),

        val suggestedAuthors: List<String> = emptyList(),
        val suggestedTranslators: List<String> = emptyList(),

        val searchQuery: String = "",
        val isSearchExpanded: Boolean = false,
        val activeTokens: List<SearchToken> = emptyList(),

        val sortOrder: SortOrder = SortOrder.DESCENDING,
        val sortBy: SortBy = SortBy.EMPTY,
        val selectedLang: String? = null,

        val recentSearches: List<SearchToken> = emptyList()
    )

    sealed interface Intent {
        data object EnterDeleteMode : Intent
        data object ExitDeleteMode : Intent
        data object DeleteSelectedGames : Intent
        data class ToggleGameSelection(val gameId: Long) : Intent
        data class AddAllVisibleGames(val gameIds: Set<Long>) : Intent
        data class UpdateQuery(val query: String) : Intent
        data class ToggleSearch(val isExpanded: Boolean) : Intent
        data class AddToken(val token: SearchToken) : Intent
        data class RemoveToken(val token: SearchToken) : Intent
        data object ToggleSortOrder : Intent
        data class SelectLanguage(val lang: String?) : Intent
        data class SelectSort(val sortBy: SortBy) : Intent
        data object ExecuteSearch : Intent
    }

    sealed interface Message {
        data object ClearSelection : Message
        data class ChangeStateDeleteMode(val newState: Boolean) : Message
        data class SelectionUpdated(val newIds: Set<Long>) : Message
        data class ChangeSortOrder(val newOrder: SortOrder) : Message
        data class ChangeSortBy(val newSort: SortBy) : Message
        data class ChangeSortLang(val newLang: String?) : Message
        data class ChangeStateSearch(val newState: Boolean) : Message
        data class UpdateSearchQuery(val newQuery: String) : Message
        data class UpdateActiveTokens(val newTokens: List<SearchToken>) : Message
        data class UpdateRecentSearches(val newSearches: List<SearchToken>) : Message
    }

    sealed interface Label

    sealed interface Action {
        data object StartSyncDictionary : Action
    }
}