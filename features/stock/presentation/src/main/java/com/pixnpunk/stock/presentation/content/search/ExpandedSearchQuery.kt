package com.pixnpunk.stock.presentation.content.search

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import com.pixnpunk.stock.domain.model.SearchToken
import com.pixnpunk.stock.domain.model.SearchTokenType

fun LazyListScope.expandedSearchQueryEmpty(
    recentSearches: List<SearchToken>,
    suggestedAuthors: List<String>,
    suggestedTranslators: List<String>,
    addToken: (SearchToken) -> Unit
) {
    if (recentSearches.isNotEmpty()) {
        item { SearchSectionHeader("Recent") }
        items(recentSearches) { token ->
            SearchSuggestionItem(
                token = token,
                icon = Icons.Default.History,
                onClick = { addToken(token) }
            )
        }
    }

    if (suggestedAuthors.isNotEmpty()) {
        item { SearchSectionHeader("Random authors") }
        items(suggestedAuthors) { author ->
            val token = SearchToken(
                value = author,
                type = SearchTokenType.AUTHOR,
                displayText = author
            )
            SearchSuggestionItem(
                token = token,
                icon = Icons.Default.Person,
                onClick = { addToken(token) }
            )
        }
    }

    if (suggestedTranslators.isNotEmpty()) {
        item { SearchSectionHeader("Random translators") }
        items(suggestedTranslators) { translator ->
            val token = SearchToken(
                value = translator,
                type = SearchTokenType.TRANSLATOR,
                displayText = translator
            )
            SearchSuggestionItem(
                token = token,
                icon = Icons.Default.Translate,
                onClick = { addToken(token) }
            )
        }
    }
}

fun LazyListScope.expandedSearchQueryNotEmpty(
    suggestedAuthors: List<String>,
    suggestedTranslators: List<String>,
    searchQuery: String,
    addToken: (SearchToken) -> Unit
) {
    item { SearchSectionHeader("Search by text") }
    item {
        val titleToken = SearchToken(
            searchQuery,
            SearchTokenType.TITLE,
            "In title: $searchQuery"
        )
        SearchSuggestionItem(
            token = titleToken,
            icon = Icons.Default.Book,
            onClick = { addToken(titleToken) }
        )
    }

    item {
        val descToken = SearchToken(
            value = searchQuery,
            type = SearchTokenType.DESCRIPTION,
            displayText = "In description: $searchQuery"
        )
        SearchSuggestionItem(
            token = descToken,
            icon = Icons.AutoMirrored.Filled.Subject,
            onClick = { addToken(descToken) }
        )
    }

    if (suggestedAuthors.isNotEmpty()) {
        item { SearchSectionHeader("Authors") }
        items(suggestedAuthors) { author ->
            val token = SearchToken(
                value = author,
                type = SearchTokenType.AUTHOR,
                displayText = author
            )
            SearchSuggestionItem(
                token = token,
                icon = Icons.Default.Person,
                onClick = { addToken(token) }
            )
        }
    }

    if (suggestedTranslators.isNotEmpty()) {
        item { SearchSectionHeader("Translators") }
        items(suggestedTranslators) { translator ->
            val token = SearchToken(
                value = translator,
                type = SearchTokenType.TRANSLATOR,
                displayText = translator
            )
            SearchSuggestionItem(
                token = token,
                icon = Icons.Default.Translate,
                onClick = { addToken(token) }
            )
        }
    }
}