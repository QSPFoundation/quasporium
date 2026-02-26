package com.pixnpunk.details.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Store
import com.pixnpunk.dto.GameEntry
import kotlinx.serialization.Serializable

sealed interface DetailsStore : Store<DetailsStore.Intent, DetailsStore.State, DetailsStore.Label> {
    @Serializable
    data class State(
        val gameEntry: GameEntry
    )

    sealed interface Intent

    sealed interface Message {
        data class UpdateEntry(val entry: GameEntry) : Message
    }

    sealed interface Label

    sealed interface Action {
        data class StartEntryFlow(val entry: GameEntry) : Action
    }
}