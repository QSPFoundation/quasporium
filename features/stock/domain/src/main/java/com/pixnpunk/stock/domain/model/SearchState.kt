package com.pixnpunk.stock.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchState(
    val query: String = "",
    val sortBy: SortBy = SortBy.EMPTY,
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val selectedLang: String? = null,
    val activeTokens: List<SearchToken> = emptyList(),
)
