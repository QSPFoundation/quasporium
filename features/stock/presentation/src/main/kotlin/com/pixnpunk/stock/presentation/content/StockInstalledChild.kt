package com.pixnpunk.stock.presentation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.pixnpunk.dto.BinaryPrefFormat
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.presentation.R
import com.pixnpunk.stock.presentation.StockComponent
import com.pixnpunk.stock.presentation.content.paging.AppendErrorItem
import com.pixnpunk.stock.presentation.content.paging.StockAppendLoadingItem
import com.pixnpunk.stock.presentation.content.paging.StockFullScreenError
import com.pixnpunk.stock.presentation.content.paging.StockFullScreenLoading

@Composable
fun StockInstalledChild(
    component: StockComponent,
    prefFormat: BinaryPrefFormat,
    listState: LazyListState,
    lazyPagingItems: LazyPagingItems<GameEntry>
) {
    val model by component.model.collectAsState()

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
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    count = lazyPagingItems.itemCount,
                    key = lazyPagingItems.itemKey { it.id },
                    contentType = lazyPagingItems.itemContentType { "InstalledGame" }
                ) { index ->
                    val gameEntry = lazyPagingItems[index]
                    if (gameEntry != null) {
                        StockInstalledGameListItem(
                            gameEntry = gameEntry,
                            prefFormat = prefFormat,
                            isSelected = model.selectedGameIds.contains(gameEntry.id),
                            deleteModeActive = model.isDeleteModeActive,
                            onClick = {
                                if (model.isDeleteModeActive) {
                                    component.toggleGameSelection(gameEntry.id)
                                } else {
                                    component.navigateToDetails(gameEntry)
                                }
                            },
                            onLongClick = {
                                if (!model.isDeleteModeActive) {
                                    component.enterDeleteMode()
                                    component.toggleGameSelection(gameEntry.id)
                                }
                            }
                        )
                    }
                }

                lazyPagingItems.loadState.append.let { appendState ->
                    when (appendState) {
                        is LoadState.Loading -> {
                            item { StockAppendLoadingItem() }
                        }

                        is LoadState.Error -> {
                            item {
                                AppendErrorItem(
                                    message = stringResource(R.string.games_list_error_append),
                                    onRetry = { lazyPagingItems.retry() }
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }
        }

        when {
            isFirstLoad -> StockFullScreenLoading()

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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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