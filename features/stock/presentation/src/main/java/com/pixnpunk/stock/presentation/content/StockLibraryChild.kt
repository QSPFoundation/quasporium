package com.pixnpunk.stock.presentation.content

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Compact
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.pixnpunk.stock.presentation.StockComponent
import com.pixnpunk.stock.presentation.content.paging.AppendErrorItem
import com.pixnpunk.stock.presentation.content.paging.StockAppendLoadingItem
import com.pixnpunk.stock.presentation.content.paging.StockFullScreenError
import com.pixnpunk.stock.presentation.content.paging.StockFullScreenLoading

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun StockLibraryChild(
    component: StockComponent,
    numCountInfo: Int,
    gridState: LazyGridState
) {
    val lazyPagingItems = component.gameRemotePageFlow.collectAsLazyPagingItems()

    val activity = LocalActivity.current
    val sizeClass = if (activity != null) {
        calculateWindowSizeClass(activity)
    } else {
        val containerSize = LocalWindowInfo.current.containerSize
        val size = DpSize(containerSize.width.dp, containerSize.height.dp)
        WindowSizeClass.calculateFromSize(size)
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(if (sizeClass.widthSizeClass > Compact) 4 else 2)
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