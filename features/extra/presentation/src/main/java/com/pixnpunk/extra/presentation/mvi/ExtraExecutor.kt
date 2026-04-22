package com.pixnpunk.extra.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.extra.presentation.mvi.ExtraStore.Message.UpdateExtraDesc
import com.pixnpunk.game.data.GameNativeEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExtraExecutor(
    private val gameNativeEngine: GameNativeEngine,
) : CoroutineExecutor<ExtraStore.Intent, ExtraStore.Action, ExtraStore.State, ExtraStore.Message, ExtraStore.Label>() {

    override fun executeAction(action: ExtraStore.Action) {
        when (action) {
            is ExtraStore.Action.StartExtraReceiveFlow -> {
                scope.launch(Dispatchers.IO) {
                    gameNativeEngine.gameStateFlow.collect { state ->
                        scope.launch { dispatch(UpdateExtraDesc(state.varsDesc)) }
                    }
                }
            }
        }
    }
}