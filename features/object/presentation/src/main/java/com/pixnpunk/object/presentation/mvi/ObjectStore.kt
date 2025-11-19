package com.pixnpunk.`object`.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Store
import kotlinx.serialization.Serializable
import com.pixnpunk.dto.LibGenItem

sealed interface ObjectStore : Store<ObjectStore.Intent, ObjectStore.State, ObjectStore.Label> {
    @Serializable
    data class State(
        val objects: List<LibGenItem> = listOf(LibGenItem()),
    )

    sealed interface Intent {
        data class ObjectSelected(val index: Int) : Intent
    }

    sealed interface Message {
        data class UpdateObjects(val objects: List<LibGenItem>) : Message
    }

    sealed interface Label

    sealed interface Action {
        data object StartObjectsFlow : Action
    }
}