package com.pixnpunk.stock.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pixnpunk.stock.domain.model.SearchToken
import com.pixnpunk.stock.domain.model.SortBy
import com.pixnpunk.stock.domain.model.SortOrder
import com.pixnpunk.stock.presentation.R
import com.pixnpunk.stock.presentation.TabState
import com.pixnpunk.stock.presentation.content.filter.filterItemCategory
import com.pixnpunk.stock.presentation.content.filter.filterItemLang
import com.pixnpunk.stock.presentation.content.filter.filterItemOrder
import com.pixnpunk.stock.presentation.content.filter.filterItemToken
import com.pixnpunk.stock.presentation.content.filter.filterVerticalDivider
import com.pixnpunk.stock.presentation.content.search.SearchInputField
import com.pixnpunk.stock.presentation.content.search.expandedSearchQueryEmpty
import com.pixnpunk.stock.presentation.content.search.expandedSearchQueryNotEmpty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockSearchAppBar(
    pagerState: PagerState,
    sortOrder: SortOrder,
    sortBy: SortBy,
    lang: String?,
    searchQuery: String,
    isSearchExpanded: Boolean,
    activeTokens: List<SearchToken>,
    recentSearches: List<SearchToken>,
    suggestedAuthors: List<String>,
    suggestedTranslators: List<String>,
    updateSearchQuery: (String) -> Unit,
    executeSearch: () -> Unit,
    toggleSearch: (Boolean) -> Unit,
    addToken: (SearchToken) -> Unit,
    removeToken: (SearchToken) -> Unit,
    selectSortOrder: () -> Unit,
    selectSortBy: (SortBy) -> Unit,
    selectSortLang: (String?) -> Unit,
    navigateToSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var canRequestFocus by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        canRequestFocus = true
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .focusProperties { canFocus = canRequestFocus },
            inputField = {
                SearchInputField(
                    searchQuery = searchQuery,
                    isSearchExpanded = isSearchExpanded,
                    updateSearchQuery = updateSearchQuery,
                    executeSearch = executeSearch,
                    toggleSearch = toggleSearch,
                    navigateToSettings = navigateToSettings
                )
            },
            expanded = isSearchExpanded,
            onExpandedChange = { toggleSearch(it) }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    expandedSearchQueryEmpty(
                        recentSearches = recentSearches,
                        suggestedAuthors = suggestedAuthors,
                        suggestedTranslators = suggestedTranslators,
                        addToken = addToken
                    )
                } else {
                    expandedSearchQueryNotEmpty(
                        suggestedAuthors = suggestedAuthors,
                        suggestedTranslators = suggestedTranslators,
                        searchQuery = searchQuery,
                        addToken = addToken
                    )
                }
            }
        }

        PrimaryTabRow(
            modifier = Modifier.padding(top = 10.dp),
            selectedTabIndex = pagerState.currentPage,
        ) {
            TabState.entries.forEachIndexed { index, state ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            text = when (state.ordinal) {
                                TabState.INSTALLED.ordinal -> stringResource(R.string.first_tab_text)
                                TabState.LIBRARY.ordinal -> stringResource(R.string.second_tab_text)
                                else -> ""
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                )
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (activeTokens.isNotEmpty()) {
                filterItemToken(
                    activeTokens = activeTokens,
                    removeToken = removeToken
                )
                filterVerticalDivider()
            }

            filterItemOrder(
                sortOrder = sortOrder,
                toggleSortOrder = selectSortOrder
            )

            filterItemLang(
                selectedLang = lang,
                selectLang = selectSortLang
            )

            filterVerticalDivider()

            filterItemCategory(
                sortBy = sortBy,
                selectSort = selectSortBy
            )
        }
    }
}