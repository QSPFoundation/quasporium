package com.pixnpunk.stock.presentation.content.search

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixnpunk.stock.domain.model.SearchToken

@Composable
fun ColumnScope.SharedSearchSuggestionColumn(
    suggestedAuthors: List<String>,
    suggestedTranslators: List<String>,
    searchQuery: String,
    recentSearches: List<SearchToken>,
    updateSearchQuery: (String) -> Unit,
    executeSearch: () -> Unit,
    addToken: (SearchToken) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        when {
            searchQuery.length >= 3 -> {
                expandedSearchQueryNotEmpty(
                    suggestedAuthors = suggestedAuthors,
                    suggestedTranslators = suggestedTranslators,
                    searchQuery = searchQuery,
                    addSearch = {
                        updateSearchQuery(it)
                        executeSearch()
                    },
                    addToken = addToken
                )
            }

            searchQuery.isNotEmpty() && searchQuery.length < 3 -> {
                expandedSearchQueryEmpty(
                    suggestedAuthors = suggestedAuthors,
                    suggestedTranslators = suggestedTranslators,
                    searchQuery = searchQuery,
                    addToken = addToken
                )
            }

            else -> {
                expandedSearchQueryDefault(
                    suggestedAuthors = suggestedAuthors,
                    suggestedTranslators = suggestedTranslators,
                    searchQuery = searchQuery,
                    recentSearches = recentSearches,
                    addSearch = updateSearchQuery,
                    addToken = addToken
                )
            }
        }
    }
}