package com.pixnpunk.stock.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.collectAsLazyPagingItems
import com.pixnpunk.stock.presentation.content.StockDeleteAppBar
import com.pixnpunk.stock.presentation.content.StockFloatButton
import com.pixnpunk.stock.presentation.content.StockInstalledChild
import com.pixnpunk.stock.presentation.content.StockLibraryChild
import com.pixnpunk.stock.presentation.content.StockSearchAppBar
import com.pixnpunk.stock.presentation.model.FabType
import com.pixnpunk.stock.presentation.model.TabState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockContent(
    component: StockComponent
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val model by component.model.collectAsState()
    val settings by component.settingsState.collectAsState()
    val authorSuggestions by component.authorSuggestionsFlow.collectAsState(emptyList())
    val translatorSuggestions by component.translatorSuggestionsFlow.collectAsState(emptyList())
    val lazyLocalPagingItems = component.gameLocalPageFlow.collectAsLazyPagingItems()

    val scrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()

    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()
    val pagerState = rememberPagerState { TabState.entries.size }

    val isAtTop by remember { derivedStateOf { !gridState.canScrollBackward } }

    val isAnyScrollInProgress =
        gridState.isScrollInProgress ||
        listState.isScrollInProgress ||
        pagerState.isScrollInProgress
    LaunchedEffect(isAnyScrollInProgress) {
        if (isAnyScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (!model.isDeleteModeActive) {
                StockSearchAppBar(
                    pagerState = pagerState,
                    sortOrder = model.sortOrder,
                    selectSortOrder = component::changeSortOrder,
                    sortBy = model.sortBy,
                    selectSortBy = component::changeSortBy,
                    lang = model.selectedLang,
                    selectSortLang = component::changeSortLang,
                    searchQuery = model.searchQuery,
                    isSearchExpanded = model.isSearchExpanded,
                    activeTokens = model.activeTokens,
                    recentSearches = model.recentSearches,
                    suggestedAuthors = authorSuggestions,
                    suggestedTranslators = translatorSuggestions,
                    toggleSearch = component::toggleSearch,
                    updateSearchQuery = component::updateSearchQuery,
                    executeSearch = component::executeSearch,
                    addToken = component::addToken,
                    removeToken = component::removeToken,
                    navigateToSettings = component.navigateToSettings
                )
            } else {
                StockDeleteAppBar(
                    deleteSetSize = model.selectedGameIds.size,
                    stopDeleteMode = component::exitDeleteMode,
                    selectAll = {
                        component.addAllGames(
                            lazyLocalPagingItems
                                .itemSnapshotList
                                .items
                                .map { it.id }
                                .toSet()
                        )
                    },
                    deleteEntries = component::deleteEntries,
                )
            }
        },
        floatingActionButton = {
            val currentTab = TabState.entries.getOrNull(pagerState.currentPage)
            val fabType = when {
                model.isDeleteModeActive -> null
                currentTab == TabState.INSTALLED -> FabType.ADD
                currentTab == TabState.LIBRARY && !isAtTop -> FabType.SCROLL_TO_TOP
                else -> null
            }

            AnimatedContent(
                targetState = fabType,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "FabTransition"
            ) { target ->
                when (target) {
                    null -> Unit

                    FabType.ADD -> StockFloatButton(
                        onClick = component.showDialogAdd,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(R.string.add_game_button_description)
                            )
                        }
                    )

                    FabType.SCROLL_TO_TOP -> StockFloatButton(
                        onClick = {
                            scope.launch {
                                launch { gridState.animateScrollToItem(0) }
                                launch {
                                    Animatable(
                                        scrollBehavior.state.heightOffset
                                    ).animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 200)
                                    ) {
                                        scrollBehavior.state.heightOffset = value
                                    }
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.ArrowUpward,
                                contentDescription = stringResource(R.string.scroll_to_top_button_description)
                            )
                        }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        HorizontalPager(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalAlignment = Alignment.Top,
            state = pagerState,
            key = { TabState.entries[it] }
        ) {
            when (it) {
                TabState.LIBRARY.ordinal ->
                    StockLibraryChild(
                        component = component,
                        prefFormat = settings.binaryPref,
                        gridState = gridState
                    )

                TabState.INSTALLED.ordinal ->
                    StockInstalledChild(
                        component = component,
                        prefFormat = settings.binaryPref,
                        listState = listState,
                        lazyPagingItems = lazyLocalPagingItems
                    )
            }
        }
    }
}
