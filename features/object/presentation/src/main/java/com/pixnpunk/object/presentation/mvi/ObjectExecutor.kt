package com.pixnpunk.`object`.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.natives.SupervisorViewModel
import com.pixnpunk.`object`.presentation.mvi.ObjectStore.Message.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ObjectExecutor(
    private val service: SupervisorViewModel
) : CoroutineExecutor<ObjectStore.Intent, ObjectStore.Action, ObjectStore.State, ObjectStore.Message, ObjectStore.Label>() {
    private val outScope = CoroutineScope(Dispatchers.Default)

    override fun executeIntent(intent: ObjectStore.Intent) {
        when (intent) {
            is ObjectStore.Intent.ObjectSelected -> {
                service.onObjectSelected(intent.index)
            }
        }
    }

    override fun executeAction(action: ObjectStore.Action) {
        when (action) {
            is ObjectStore.Action.StartObjectsFlow -> {
                outScope.launch {
                    service.gameStateFlow.collect { state ->
                        scope.launch { dispatch(UpdateObjects(state.objectsList)) }
                    }
                }
            }
        }
    }

    override fun dispose() {
        super.dispose()
        outScope.cancel()
    }
}