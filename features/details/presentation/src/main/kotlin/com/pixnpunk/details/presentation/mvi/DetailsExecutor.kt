package com.pixnpunk.details.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.dto.GameListType
import com.pixnpunk.stock.domain.usecase.ObserveEntryByIdUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailsExecutor(
    private val observeEntryByIdUseCase: ObserveEntryByIdUseCase
) : CoroutineExecutor<DetailsStore.Intent, DetailsStore.Action, DetailsStore.State, DetailsStore.Message, DetailsStore.Label>() {

    override fun executeAction(action: DetailsStore.Action) {
        when (action) {
            is DetailsStore.Action.StartEntryFlow -> {
                if (action.entry.listId == GameListType.Playable) {
                    scope.launch(Dispatchers.IO) {
                        observeEntryByIdUseCase.execute(action.entry.id).collect { entry ->
                            if (entry != null) {
                                scope.launch { dispatch(DetailsStore.Message.UpdateEntry(entry)) }
                            } else {
                                scope.launch { publish(DetailsStore.Label.NullUpdateReceived) }
                            }
                        }
                    }
                }
            }
        }
    }
}
