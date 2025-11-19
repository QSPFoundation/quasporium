package com.pixnpunk.main.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Store
import kotlinx.serialization.Serializable
import com.pixnpunk.dto.LibGenItem

sealed interface MainStore : Store<MainStore.Intent, MainStore.State, MainStore.Label> {
    @Serializable
    data class State(
        val mainDesc: String = "",
        val isActionsVis: Boolean = true,
        val actions: List<LibGenItem> = listOf(LibGenItem()),
    )

    sealed interface Intent {
        data class ActionClick(val index: Int) : Intent
    }

    sealed interface Message {
        data class UpdateMainDesc(val description: String) : Message
        data class UpdateActions(val actions: List<LibGenItem>) : Message
        data class UpdateVisActions(val isActionsVis: Boolean) : Message
    }

    sealed interface Label

    sealed interface Action {
        data object StartActionsVisFlow : Action
        data object StartContentFlow : Action
    }
}