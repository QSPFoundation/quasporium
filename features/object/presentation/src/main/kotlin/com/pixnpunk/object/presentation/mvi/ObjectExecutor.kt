package com.pixnpunk.`object`.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.game.data.GameNativeEngine
import com.pixnpunk.`object`.presentation.mvi.ObjectStore.Message.UpdateObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ObjectExecutor(
    private val gameNativeEngine: GameNativeEngine
) : CoroutineExecutor<ObjectStore.Intent, ObjectStore.Action, ObjectStore.State, ObjectStore.Message, ObjectStore.Label>() {

    override fun executeIntent(intent: ObjectStore.Intent) {
        when (intent) {
            is ObjectStore.Intent.ObjectSelected -> {
                gameNativeEngine.onObjectSelected(intent.index)
            }
        }
    }

    override fun executeAction(action: ObjectStore.Action) {
        when (action) {
            is ObjectStore.Action.StartObjectsFlow -> {
                scope.launch(Dispatchers.IO) {
                    gameNativeEngine.gameStateFlow.collect { state ->
                        scope.launch { dispatch(UpdateObjects(state.objectsList)) }
                    }
                }
            }
        }
    }
}
