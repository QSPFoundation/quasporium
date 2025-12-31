package com.pixnpunk.stock.presentation.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.pixnpunk.stock.presentation.StockComponent
import com.pixnpunk.stock.presentation.content.paging.AppendErrorItem
import com.pixnpunk.stock.presentation.content.paging.StockAppendLoadingItem
import com.pixnpunk.stock.presentation.content.paging.StockFullScreenError
import com.pixnpunk.stock.presentation.content.paging.StockFullScreenLoading

@Composable
fun StockLibraryChild(
    component: StockComponent,
    numCountInfo: Int,
    gridState: LazyGridState
) {
    val lazyPagingItems = component.gameRemotePageFlow.collectAsLazyPagingItems()

    BoxWithConstraints {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(
                when {
                    maxWidth < WIDTH_DP_MEDIUM_LOWER_BOUND.dp -> 2
                    maxWidth < WIDTH_DP_EXPANDED_LOWER_BOUND.dp -> 3
                    else -> 4
                }
            )
        ) {
            items(
                count = lazyPagingItems.itemCount
            ) { index ->
                lazyPagingItems[index]?.let { gameEntry ->
                    StockLibraryGameGridItem(
                        gameEntry = gameEntry,
                        numCountInfo = numCountInfo,
                        onClick = { component.navigateToDetails(gameEntry) }
                    )
                }
            }

            lazyPagingItems.loadState.append.let { appendState ->
                when (appendState) {
                    is LoadState.Loading -> {
                        item {
                            StockAppendLoadingItem()
                        }
                    }

                    is LoadState.Error -> {
                        item {
                            AppendErrorItem(
                                message = "Failed to load more games.",
                                onRetry = { lazyPagingItems.retry() }
                            )
                        }
                    }

                    is LoadState.NotLoading -> {
                        // do nothing
                    }
                }
            }
        }
    }

    when (lazyPagingItems.loadState.refresh) {
        is LoadState.Loading -> {
            StockFullScreenLoading()
        }

        is LoadState.Error -> {
            StockFullScreenError(
                message = "No Internet Connection",
                onRetry = { lazyPagingItems.retry() }
            )
        }

        is LoadState.NotLoading -> {
            if (lazyPagingItems.itemCount == 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Games Found")
                }
            }
        }
    }
}