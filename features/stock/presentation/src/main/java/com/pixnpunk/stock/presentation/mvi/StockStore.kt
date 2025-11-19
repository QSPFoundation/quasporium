package com.pixnpunk.stock.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Store
import com.pixnpunk.dto.GameEntry
import kotlinx.serialization.Serializable

sealed interface StockStore : Store<StockStore.Intent, StockStore.State, StockStore.Label> {
    @Serializable
    data class State(
        val isDeleteModeActive: Boolean = false
    )

    sealed interface Intent {
        data object StartDeleteMode : Intent
        data object StopDeleteMode : Intent
        data class DeleteEntries(val entries: Set<GameEntry>) : Intent
    }

    sealed interface Message {
        data class ChangeStateDeleteMode(val newState: Boolean) : Message
    }

    sealed interface Label

    sealed interface Action
}