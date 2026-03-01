package com.pixnpunk.game.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Reducer

object GameReducer : Reducer<GameStore.State, GameStore.Message> {
    override fun GameStore.State.reduce(msg: GameStore.Message): GameStore.State {
        return when (msg) {
            is GameStore.Message.UpdateGameStatus -> copy(isGameRunning = msg.isGameRunning)
            is GameStore.Message.UpdateVisExtraElement -> copy(isExtraEnabled = msg.isExtraEnabled)
            is GameStore.Message.UpdateStatExtraElement -> copy(isExtraUpdated = msg.isExtraUpdated)
            is GameStore.Message.UpdateVisInputElement -> copy(isInputEnabled = msg.isInputEnabled)
            is GameStore.Message.UpdateVisObjElement -> copy(isObjectsEnabled = msg.isObjectsEnabled)
            is GameStore.Message.UpdateStatObjElement -> copy(isObjectsUpdated = msg.isObjectsUpdated)
            is GameStore.Message.UpdateExecutorStatus -> copy(isExecutorEnabled = msg.isExecutorEnabled)
        }
    }
}