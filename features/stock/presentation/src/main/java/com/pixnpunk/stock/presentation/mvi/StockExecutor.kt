package com.pixnpunk.stock.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.stock.domain.usecase.DeleteLocalGameEntries
import com.pixnpunk.stock.presentation.mvi.StockStore.Message.ChangeStateDeleteMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StockExecutor(
    private val deleteLocalGameEntries: DeleteLocalGameEntries
) : CoroutineExecutor<StockStore.Intent, StockStore.Action, StockStore.State, StockStore.Message, StockStore.Label>() {

    override fun executeIntent(intent: StockStore.Intent) {
        when (intent) {
            is StockStore.Intent.StartDeleteMode -> {
                dispatch(ChangeStateDeleteMode(true))
            }

            is StockStore.Intent.StopDeleteMode -> {
                dispatch(ChangeStateDeleteMode(false))
            }

            is StockStore.Intent.DeleteEntries -> {
                scope.launch {
                    dispatch(ChangeStateDeleteMode(false))
                    withContext(Dispatchers.IO) { deleteLocalGameEntries.execute(intent.entries) }
                }
            }
        }
    }

    override fun executeAction(action: StockStore.Action) {
    }
}