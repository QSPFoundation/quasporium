package com.pixnpunk.extra.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Store
import kotlinx.serialization.Serializable

interface ExtraStore : Store<ExtraStore.Intent, ExtraStore.State, ExtraStore.Label> {
    @Serializable
    data class State(
        val extraDesc: String = "",
    )

    sealed interface Intent

    sealed interface Message {
        data class UpdateExtraDesc(val description: String) : Message
    }

    sealed interface Label

    sealed interface Action {
        data object StartExtraReceiveFlow : Action
    }
}