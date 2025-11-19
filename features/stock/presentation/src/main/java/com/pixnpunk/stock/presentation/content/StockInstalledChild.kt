package com.pixnpunk.stock.presentation.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.presentation.StockComponent
import com.pixnpunk.stock.presentation.content.paging.AppendErrorItem
import com.pixnpunk.stock.presentation.content.paging.StockAppendLoadingItem
import com.pixnpunk.stock.presentation.content.paging.StockFullScreenError
import com.pixnpunk.stock.presentation.content.paging.StockFullScreenLoading

@Composable
fun StockInstalledChild(
    component: StockComponent,
    numCountInfo: Int,
    listState: LazyListState,
    setToDelete: SnapshotStateSet<GameEntry>,
    allEntries: SnapshotStateSet<GameEntry>,
    deleteModeActive: Boolean
) {
    val lazyPagingItems = component.gameLocalPageFlow.collectAsLazyPagingItems()

    if (deleteModeActive) {
        allEntries.addAll(lazyPagingItems.itemSnapshotList.items)
    }

    LazyColumn(state = listState) {
        items(
            count = lazyPagingItems.itemCount,
            key = lazyPagingItems.itemKey { it.id }
        ) { index ->
            lazyPagingItems[index]?.let { gameEntry ->
                StockInstalledGameListItem(
                    gameEntry = gameEntry,
                    numCountInfo = numCountInfo,
                    isSelected = setToDelete.contains(gameEntry),
                    deleteModeActive = deleteModeActive,
                    onToggleSelection = {
                        if (setToDelete.contains(gameEntry)) {
                            setToDelete.remove(gameEntry)
                        } else {
                            setToDelete.add(gameEntry)
                        }
                    },
                    onClick = { component.navigateToDetails(gameEntry) },
                    onLongClick = { component.startDeleteMode() }
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

    when (val refreshState = lazyPagingItems.loadState.refresh) {
        is LoadState.Loading -> {
            StockFullScreenLoading()
        }

        is LoadState.Error -> {
            StockFullScreenError(
                message = "An error occurred: ${refreshState.error.message}",
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