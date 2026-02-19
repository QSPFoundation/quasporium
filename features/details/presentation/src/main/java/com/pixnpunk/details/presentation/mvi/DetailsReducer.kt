package com.pixnpunk.details.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Reducer

object DetailsReducer : Reducer<DetailsStore.State, DetailsStore.Message> {
    override fun DetailsStore.State.reduce(msg: DetailsStore.Message): DetailsStore.State {
        return when (msg) {
            is DetailsStore.Message.UpdateEntry -> copy(gameEntry = msg.entry)
        }
    }
}