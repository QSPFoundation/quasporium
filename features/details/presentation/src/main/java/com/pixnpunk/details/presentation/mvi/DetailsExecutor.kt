package com.pixnpunk.details.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.stock.domain.usecase.ObserveEntryByIdUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailsExecutor(
    private val observeEntryByIdUseCase: ObserveEntryByIdUseCase
) : CoroutineExecutor<DetailsStore.Intent, DetailsStore.Action, DetailsStore.State, DetailsStore.Message, DetailsStore.Label>() {
    override fun executeIntent(intent: DetailsStore.Intent) {
        when (intent) {
            is DetailsStore.Intent.SubscribeEntry ->
                scope.launch(Dispatchers.IO) {
                    observeEntryByIdUseCase.execute(intent.entryId).collect { entry ->
                        scope.launch { dispatch(DetailsStore.Message.UpdateEntry(entry)) }
                    }
                }
        }
    }
}