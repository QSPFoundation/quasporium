package com.pixnpunk.stock.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Store
import kotlinx.serialization.Serializable

sealed interface StockStore : Store<StockStore.Intent, StockStore.State, StockStore.Label> {
    @Serializable
    data class State(
        val isDeleteModeActive: Boolean = false,
        val selectedGameIds: Set<Long> = emptySet()
    )

    sealed interface Intent {
        data object EnterDeleteMode : Intent
        data object ExitDeleteMode : Intent
        data object DeleteSelectedGames : Intent
        data class ToggleGameSelection(val gameId: Long) : Intent
        data class AddAllVisibleGames(val gameIds: Set<Long>) : Intent
    }

    sealed interface Message {
        data object ClearSelection : Message
        data class ChangeStateDeleteMode(val newState: Boolean) : Message
        data class SelectionUpdated(val newIds: Set<Long>) : Message
    }

    sealed interface Label

    sealed interface Action
}