package com.pixnpunk.details.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Store
import com.pixnpunk.details.presentation.model.GameFileItem
import com.pixnpunk.details.presentation.model.PresetUIState
import com.pixnpunk.dto.GameEntry
import kotlinx.serialization.Serializable

sealed interface DetailsStore : Store<DetailsStore.Intent, DetailsStore.State, DetailsStore.Label> {
    @Serializable
    data class State(
        val gameEntry: GameEntry,
        val gameProfiles: List<PresetUIState> = emptyList(),
        val gameFiles: List<GameFileItem> = emptyList()
    )

    sealed interface Intent {
        data object CreateProfile : Intent
        data class UpdateProfileVisuals(val presetId: Long, val newName: String, val newColor: Long) : Intent
        data class SetBackgroundImage(val presetId: Long, val uriString: String) : Intent
        data class RemoveBackgroundImage(val presetId: Long) : Intent
        data class DeleteProfile(val presetId: Long) : Intent
        data class StartFresh(val presetId: Long, val fileName: String) : Intent
        data class ContinueGame(val presetId: Long, val savePath: String) : Intent
    }

    sealed interface Message {
        data class UpdateEntry(val entry: GameEntry) : Message
        data class UpdateProfiles(val profiles: List<PresetUIState>) : Message
        data class UpdateGameFiles(val files: List<GameFileItem>) : Message
    }

    sealed interface Label {
        data object NullUpdateReceived : Label
        data class LaunchEngine(val presetId: Long, val entryFile: String, val saveFile: String? = null) : Label
    }

    sealed interface Action {
        data class Init(val entry: GameEntry) : Action
    }
}
