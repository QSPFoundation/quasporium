package com.pixnpunk.game.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Reducer

object GameReducer : Reducer<GameStore.State, GameStore.Message> {
    override fun GameStore.State.reduce(msg: GameStore.Message): GameStore.State {
        return when (msg) {
            is GameStore.Message.UpdateGameStatus -> copy(isGameRunning = msg.isGameRunning)
            is GameStore.Message.UpdateVisExtraElement -> copy(isExtraEnabled = msg.isExtraEnabled)
            is GameStore.Message.UpdateVisInputElement -> copy(isInputEnabled = msg.isInputEnabled)
            is GameStore.Message.UpdateVisObjElement -> copy(isObjectsEnabled = msg.isObjectsEnabled)
            is GameStore.Message.UpdateExecutorStatus -> copy(isExecutorEnabled = msg.isExecutorEnabled)
            is GameStore.Message.UpdateIsExtraDescUpdated -> copy(isExtraDescUpdated = msg.isExtraDescUpdated)
            is GameStore.Message.UpdateIsMainDescUpdated -> copy(isMainDescUpdated = msg.isMainDescUpdated)
            is GameStore.Message.UpdateIsObjsListUpdated -> copy(isObjectsListUpdated = msg.isObjsListUpdated)
        }
    }
}