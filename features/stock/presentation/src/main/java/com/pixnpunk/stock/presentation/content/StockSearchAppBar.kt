package com.pixnpunk.stock.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExpandedDockedSearchBar
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
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
import com.pixnpunk.stock.presentation.content.search.SharedSearchSuggestionColumn
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

    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collect(updateSearchQuery)
    }

    LaunchedEffect(searchQuery) {
        if (textFieldState.text.toString() != searchQuery) {
            textFieldState.edit {
                replace(0, length, searchQuery)
            }
        }
    }

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded && searchBarState.currentValue == SearchBarValue.Collapsed) {
            searchBarState.animateToExpanded()
        } else if (!isSearchExpanded && searchBarState.currentValue == SearchBarValue.Expanded) {
            searchBarState.animateToCollapsed()
        }
    }

    LaunchedEffect(searchBarState.currentValue) {
        val composeExpanded = searchBarState.currentValue == SearchBarValue.Expanded
        if (composeExpanded != isSearchExpanded) {
            toggleSearch(composeExpanded)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        SearchBar(
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.CenterHorizontally)
                .focusProperties { canFocus = canRequestFocus },
            state = searchBarState,
            inputField = {
                SearchInputField(
                    searchBarState = searchBarState,
                    textFieldState = textFieldState,
                    searchQuery = searchQuery,
                    isSearchExpanded = isSearchExpanded,
                    updateSearchQuery = updateSearchQuery,
                    executeSearch = executeSearch,
                    toggleSearch = toggleSearch,
                    navigateToSettings = navigateToSettings
                )
            }
        )
        if (currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(
                WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
            )
        ) {
            ExpandedDockedSearchBar(
                state = searchBarState,
                inputField = {
                    SearchInputField(
                        searchBarState = searchBarState,
                        textFieldState = textFieldState,
                        searchQuery = searchQuery,
                        isSearchExpanded = isSearchExpanded,
                        updateSearchQuery = updateSearchQuery,
                        executeSearch = executeSearch,
                        toggleSearch = toggleSearch,
                        navigateToSettings = navigateToSettings
                    )
                },
                content = {
                    SharedSearchSuggestionColumn(
                        suggestedAuthors = suggestedAuthors,
                        suggestedTranslators = suggestedTranslators,
                        searchQuery = searchQuery,
                        recentSearches = recentSearches,
                        addToken = addToken,
                        addSearch = updateSearchQuery
                    )
                }
            )
        } else {
            ExpandedFullScreenSearchBar(
                state = searchBarState,
                inputField = {
                    SearchInputField(
                        searchBarState = searchBarState,
                        textFieldState = textFieldState,
                        searchQuery = searchQuery,
                        isSearchExpanded = isSearchExpanded,
                        updateSearchQuery = updateSearchQuery,
                        executeSearch = executeSearch,
                        toggleSearch = toggleSearch,
                        navigateToSettings = navigateToSettings
                    )
                },
                content = {
                    SharedSearchSuggestionColumn(
                        suggestedAuthors = suggestedAuthors,
                        suggestedTranslators = suggestedTranslators,
                        searchQuery = searchQuery,
                        recentSearches = recentSearches,
                        addToken = addToken,
                        addSearch = updateSearchQuery
                    )
                }
            )
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