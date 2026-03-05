package com.pixnpunk.game.presentation.mvi

import android.net.Uri
import com.arkivanov.mvikotlin.core.store.Store
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.LibTypeDialog
import kotlinx.serialization.Serializable

sealed interface GameStore : Store<GameStore.Intent, GameStore.State, GameStore.Label> {
    @Serializable
    data class State(
        val isObjectsEnabled: Boolean = true,
        val isExtraEnabled: Boolean = true,
        val isInputEnabled: Boolean = true,
        val isExecutorEnabled: Boolean = false,
        val isGameRunning: Boolean = false,

        val gameId: Long = 1L,
        val gameTitle: String = "Title",
        val gameDirUriBox: String = "",
        val gameFileUriBox: String = "",

        val mainHashCode: Int = 0,
        val extraHashCode: Int = 0,
        val objectsHashCode: Int = 0
    )

    sealed interface Intent {
        data class OnSaveFile(val fileUri: Uri) : Intent
        data class OnLoadFile(val fileUri: Uri) : Intent
        data class OnExecCode(val codeToExec: String) : Intent
        data class OnSelectMenuItem(val index: Int) : Intent
        data class OnInputValue(val state: InputState, val inputString: String) : Intent
        data object CreateSaveIntent : Intent
        data object CreateLoadIntent : Intent
        data object CleanCountMainPaneUpdates : Intent
        data object CleanCountExtraPaneUpdates : Intent
        data object CleanCountObjectsPaneUpdates : Intent
    }

    sealed interface Message {
        data class UpdateGameStatus(val isGameRunning: Boolean) : Message
        data class UpdateVisObjElement(val isObjectsEnabled: Boolean) : Message
        data class UpdateVisExtraElement(val isExtraEnabled: Boolean) : Message
        data class UpdateVisInputElement(val isInputEnabled: Boolean) : Message
        data class UpdateExecutorStatus(val isExecutorEnabled: Boolean) : Message
        data class UpdateMainHashCode(val mainHashCode: Int) : Message
        data class UpdateExtraHashCode(val extraHashCode: Int) : Message
        data class UpdateObjectsHashCode(val objectsHashCode: Int) : Message
    }

    sealed interface Label {
        data class ShowDialog(
            val inputString: String,
            val dialogState: LibTypeDialog,
            val inputListItems: List<LibGenItem>? = null
        ) : Label

        data class ShowSaveFilePicker(
            val intent: android.content.Intent
        ) : Label

        data class ShowLoadFilePicker(
            val intent: android.content.Intent
        ) : Label
    }

    sealed interface Action {
        data object StartGameStateFlow : Action
        data object StartGamePopupFlow : Action
        data object StartGameElementVisFlow : Action
        data object StartGameSettingsFlow : Action
        data object StartGameDialogFlow : Action
    }
}