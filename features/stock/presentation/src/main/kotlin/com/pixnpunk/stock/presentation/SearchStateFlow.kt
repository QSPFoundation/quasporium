package com.pixnpunk.stock.presentation

import com.pixnpunk.stock.domain.model.SearchState
import com.pixnpunk.stock.presentation.mapper.toDomain
import com.pixnpunk.stock.presentation.mvi.StockStore
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal const val SEARCH_DEBOUNCE_MS = 400L

@OptIn(FlowPreview::class)
internal fun Flow<StockStore.State>.toSearchState(): Flow<SearchState> =
    combine(
        map { it.searchQuery }
            .distinctUntilChanged()
            .debounce { query -> if (query.isEmpty()) 0L else SEARCH_DEBOUNCE_MS },
        this
    ) { query, state ->
        SearchState(
            query,
            state.sortBy,
            state.sortOrder,
            state.selectedLang,
            state.activeTokens.toDomain()
        )
    }.distinctUntilChanged()
