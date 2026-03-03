package com.pixnpunk.stock.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.stock.domain.usecase.DeleteLocalGameEntries
import com.pixnpunk.stock.presentation.mvi.StockStore.Message.ChangeStateDeleteMode
import com.pixnpunk.stock.presentation.mvi.StockStore.Message.ClearSelection
import com.pixnpunk.stock.presentation.mvi.StockStore.Message.SelectionUpdated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StockExecutor(
    private val deleteLocalGameEntries: DeleteLocalGameEntries
) : CoroutineExecutor<StockStore.Intent, StockStore.Action, StockStore.State, StockStore.Message, StockStore.Label>() {

    override fun executeIntent(intent: StockStore.Intent) {
        when (intent) {
            is StockStore.Intent.EnterDeleteMode ->
                dispatch(ChangeStateDeleteMode(true))

            is StockStore.Intent.AddAllVisibleGames ->
                dispatch(SelectionUpdated(intent.gameIds))

            is StockStore.Intent.ExitDeleteMode -> {
                dispatch(ClearSelection)
                dispatch(ChangeStateDeleteMode(false))
            }

            is StockStore.Intent.DeleteSelectedGames -> {
                val idsToDelete = state().selectedGameIds
                if (idsToDelete.isNotEmpty()) {
                    scope.launch {
                        withContext(Dispatchers.IO) { deleteLocalGameEntries.execute(idsToDelete) }
                        dispatch(ClearSelection)
                        dispatch(ChangeStateDeleteMode(false))
                    }
                }
            }

            is StockStore.Intent.ToggleGameSelection -> {
                val currentIds = state().selectedGameIds
                val id = intent.gameId

                val newIds = if (currentIds.contains(id)) {
                    currentIds - id
                } else {
                    currentIds + id
                }
                dispatch(SelectionUpdated(newIds))
            }
        }
    }
}