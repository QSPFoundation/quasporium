package com.pixnpunk.main.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Reducer

object MainReducer : Reducer<MainStore.State, MainStore.Message> {
    override fun MainStore.State.reduce(msg: MainStore.Message): MainStore.State {
        return when (msg) {
            is MainStore.Message.UpdateActions -> copy(actions = msg.actions)
            is MainStore.Message.UpdateMainDesc -> copy(mainDesc = msg.description)
            is MainStore.Message.UpdateVisActions -> copy(isActionsVis = msg.isActionsVis)
        }
    }
}