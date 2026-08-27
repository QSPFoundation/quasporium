package com.pixnpunk.stock.presentation.content.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixnpunk.stock.domain.model.SearchToken
import com.pixnpunk.stock.domain.model.SearchTokenType
import com.pixnpunk.stock.presentation.model.SearchTokenUI

fun LazyListScope.expandedSearchQueryDefault(
    searchQuery: String,
    recentSearches: List<SearchTokenUI>,
    suggestedAuthors: List<String>,
    suggestedTranslators: List<String>,
    addSearch: (String) -> Unit,
    addToken: (SearchTokenUI) -> Unit
) {
    if (recentSearches.isNotEmpty()) {
        item { SearchSectionHeader("Recent") }
        items(recentSearches) { token ->
            SearchSuggestionItem(
                token = token,
                icon = Icons.Default.History,
                onClick = {
                    if (token.type == SearchTokenType.TITLE) {
                        addSearch(token.value)
                    } else {
                        addToken(token)
                    }
                }
            )
        }
    }

    expandedSearchListsSuggestion(
        isSearch = searchQuery.isNotEmpty(),
        suggestedAuthors = suggestedAuthors,
        suggestedTranslators = suggestedTranslators,
        addToken = addToken
    )
}

fun LazyListScope.expandedSearchQueryEmpty(
    searchQuery: String,
    suggestedAuthors: List<String>,
    suggestedTranslators: List<String>,
    addToken: (SearchTokenUI) -> Unit
) {
    item {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter ${3 - searchQuery.length} more characters to search",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    expandedSearchListsSuggestion(
        isSearch = searchQuery.isNotEmpty(),
        suggestedAuthors = suggestedAuthors,
        suggestedTranslators = suggestedTranslators,
        addToken = addToken
    )
}

fun LazyListScope.expandedSearchQueryNotEmpty(
    suggestedAuthors: List<String>,
    suggestedTranslators: List<String>,
    searchQuery: String,
    addSearch: (String) -> Unit,
    addToken: (SearchTokenUI) -> Unit
) {
    item {
        val titleToken = SearchTokenUI(
            value = searchQuery,
            type = SearchTokenType.TITLE,
            displayText = "In title: $searchQuery"
        )
        SearchSuggestionItem(
            token = titleToken,
            icon = Icons.Default.Book,
            onClick = { addSearch(titleToken.value) }
        )
    }

    item {
        val descToken = SearchTokenUI(
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

    expandedSearchListsSuggestion(
        isSearch = true,
        suggestedAuthors = suggestedAuthors,
        suggestedTranslators = suggestedTranslators,
        addToken = addToken
    )
}

fun LazyListScope.expandedSearchListsSuggestion(
    isSearch: Boolean,
    suggestedAuthors: List<String>,
    suggestedTranslators: List<String>,
    addToken: (SearchTokenUI) -> Unit
) {
    if (suggestedAuthors.isNotEmpty()) {
        item {
            SearchSectionHeader(
                if (isSearch) {
                    "Authors"
                } else {
                    "Random authors"
                }
            )
        }
        items(suggestedAuthors) { author ->
            val token = SearchTokenUI(
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
        item {
            SearchSectionHeader(
                if (isSearch) {
                    "Translators"
                } else {
                    "Random translators"
                }
            )
        }
        items(suggestedTranslators) { translator ->
            val token = SearchTokenUI(
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