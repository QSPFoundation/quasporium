package com.pixnpunk.extra.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Reducer

object ExtraReducer : Reducer<ExtraStore.State, ExtraStore.Message> {
    override fun ExtraStore.State.reduce(msg: ExtraStore.Message): ExtraStore.State {
        return when (msg) {
            is ExtraStore.Message.UpdateExtraDesc -> copy(extraDesc = msg.description)
        }
    }
}