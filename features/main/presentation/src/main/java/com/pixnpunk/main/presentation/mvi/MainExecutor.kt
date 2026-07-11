package com.pixnpunk.main.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.game.data.GameNativeEngine
import com.pixnpunk.game.domain.model.LibTypeWindow
import com.pixnpunk.main.presentation.mvi.MainStore.Message.UpdateActions
import com.pixnpunk.main.presentation.mvi.MainStore.Message.UpdateVisActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class MainExecutor(
    private val gameNativeEngine: GameNativeEngine
) : CoroutineExecutor<MainStore.Intent, MainStore.Action, MainStore.State, MainStore.Message, MainStore.Label>() {

    override fun executeIntent(intent: MainStore.Intent) {
        when (intent) {
            is MainStore.Intent.ActionClick -> gameNativeEngine.onActionClicked(intent.index)
        }
    }

    override fun executeAction(action: MainStore.Action) {
        when (action) {
            is MainStore.Action.StartActionsVisFlow ->
                scope.launch(Dispatchers.IO) {
                    gameNativeEngine.gameElementVis
                        .distinctUntilChanged()
                        .filter { it.first == LibTypeWindow.ACTS }
                        .collect { scope.launch { dispatch(UpdateVisActions(it.second)) } }
                }

            is MainStore.Action.StartContentFlow ->
                scope.launch(Dispatchers.IO) {
                    gameNativeEngine.gameStateFlow
                        .collect { scope.launch { dispatch(UpdateActions(it.actionsList)) } }
                }
        }
    }
}
