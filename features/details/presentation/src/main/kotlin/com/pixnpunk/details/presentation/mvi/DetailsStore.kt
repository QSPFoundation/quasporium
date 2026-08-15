package com.pixnpunk.details.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Store
import com.pixnpunk.details.presentation.model.GameFileItem
import com.pixnpunk.details.presentation.model.ProfileUIState
import com.pixnpunk.dto.GameEntry
import kotlinx.serialization.Serializable

sealed interface DetailsStore : Store<DetailsStore.Intent, DetailsStore.State, DetailsStore.Label> {
    @Serializable
    data class State(
        val gameEntry: GameEntry,
        val gameProfiles: List<ProfileUIState> = emptyList(),
        val gameFiles: List<GameFileItem> = emptyList()
    )

    sealed interface Intent {
        data object CreateProfile : Intent
        data class StartFresh(val profileId: Long, val fileName: String) : Intent
        data class ContinueGame(val profileId: Long, val savePath: String) : Intent
    }

    sealed interface Message {
        data class UpdateEntry(val entry: GameEntry) : Message
        data class UpdateProfiles(val profiles: List<ProfileUIState>) : Message
        data class UpdateGameFiles(val files: List<GameFileItem>) : Message
    }

    sealed interface Label {
        data object NullUpdateReceived : Label
        data class LaunchEngine(
            val profileId: Long,
            val entryFile: String,
            val saveFile: String? = null
        ) : Label
    }

    sealed interface Action {
        data class Init(val entry: GameEntry) : Action
    }
}
