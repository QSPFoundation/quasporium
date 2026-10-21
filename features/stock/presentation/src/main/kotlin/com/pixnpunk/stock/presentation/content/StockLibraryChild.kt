package com.pixnpunk.stock.presentation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.pixnpunk.dto.BinaryPrefFormat
import com.pixnpunk.stock.presentation.R
import com.pixnpunk.stock.presentation.StockComponent
import com.pixnpunk.stock.presentation.content.paging.AppendErrorItem
import com.pixnpunk.stock.presentation.content.paging.StockAppendLoadingItem
import com.pixnpunk.stock.presentation.content.paging.StockFullScreenError
import com.pixnpunk.stock.presentation.content.paging.StockFullScreenLoading

@Composable
fun StockLibraryChild(
    component: StockComponent,
    prefFormat: BinaryPrefFormat,
    gridState: LazyGridState
) {
    val lazyPagingItems = component.gameRemotePageFlow.collectAsLazyPagingItems()

    val isFirstLoad =
        lazyPagingItems.loadState.refresh is LoadState.Loading && lazyPagingItems.itemCount == 0
    val isEmptyState =
        lazyPagingItems.loadState.refresh is LoadState.NotLoading && lazyPagingItems.itemCount == 0
    val isInitialError =
        lazyPagingItems.loadState.refresh is LoadState.Error && lazyPagingItems.itemCount == 0

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !isFirstLoad && !isInitialError,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    count = lazyPagingItems.itemCount,
                    key = lazyPagingItems.itemKey { game -> game.id },
                    contentType = lazyPagingItems.itemContentType { "RemoteGame" }
                ) { index ->
                    lazyPagingItems[index]?.let { gameEntry ->
                        StockLibraryGameGridItem(
                            gameEntry = gameEntry,
                            prefFormat = prefFormat,
                            onClick = { component.navigateToDetails(gameEntry) }
                        )
                    }
                }

                lazyPagingItems.loadState.append.let { appendState ->
                    when (appendState) {
                        is LoadState.Loading -> {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                StockAppendLoadingItem()
                            }
                        }

                        is LoadState.Error -> {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                AppendErrorItem(
                                    message = stringResource(R.string.games_list_error_append),
                                    onRetry = { lazyPagingItems.retry() }
                                )
                            }
                        }

                        is LoadState.NotLoading -> {}
                    }
                }
            }
        }

        when {
            isFirstLoad -> {
                StockFullScreenLoading()
            }

            isInitialError -> {
                val error = (lazyPagingItems.loadState.refresh as LoadState.Error).error
                StockFullScreenError(
                    message = stringResource(
                        R.string.games_list_error_template,
                        error.message ?: "Unknown error"
                    ),
                    onRetry = { lazyPagingItems.retry() }
                )
            }

            isEmptyState -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.games_list_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
