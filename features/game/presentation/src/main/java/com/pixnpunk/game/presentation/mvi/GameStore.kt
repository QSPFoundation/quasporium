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

        val isUserChooseSave: Boolean = false,

        val gameId: Long = 1L,
        val gameTitle: String = "Title",
        val gameDirUriBox: String = "",
        val gameFileUriBox: String = "",

        val isGameDirInternal: Boolean = false,

        val isMainDescUpdated: Pair<Int, Boolean> = 0 to false,
        val isExtraDescUpdated: Pair<Int, Boolean> = 0 to false,
        val isObjectsListUpdated: Pair<Int, Boolean> = 0 to false
    )

    sealed interface Intent {
        data class OnSaveFile(val fileUri: Uri) : Intent
        data class OnLoadFile(val fileUri: Uri) : Intent
        data class OnCorruptFile(val isCancelUser: Boolean) : Intent
        data class OnExecCode(val codeToExec: String) : Intent
        data class OnSelectMenuItem(val index: Int) : Intent
        data class OnInputValue(val state: InputState, val inputString: String) : Intent
        data object OnDismissDialog : Intent
        data object StartSaveFile : Intent
        data object StartLoadFile : Intent
        data object CleanMainSavedDesc : Intent
        data object CleanExtraSavedDesc : Intent
        data object CleanObjectsSavedList : Intent
    }

    sealed interface Message {
        data class UpdateGameStatus(val isGameRunning: Boolean) : Message
        data class UpdateVisObjElement(val isObjectsEnabled: Boolean) : Message
        data class UpdateVisExtraElement(val isExtraEnabled: Boolean) : Message
        data class UpdateVisInputElement(val isInputEnabled: Boolean) : Message
        data class UpdateExecutorStatus(val isExecutorEnabled: Boolean) : Message
        data class UpdateIsMainDescUpdated(val isMainDescUpdated: Pair<Int, Boolean>) : Message
        data class UpdateIsExtraDescUpdated(val isExtraDescUpdated: Pair<Int, Boolean>) : Message
        data class UpdateIsObjsListUpdated(val isObjsListUpdated: Pair<Int, Boolean>) : Message
        data class UpdateIsUserChooseSave(val isUserChooseSave: Boolean) : Message
        data class UpdateIsGameDirInternal(val isGameDirInternal: Boolean) : Message
    }

    sealed interface Label {
        data class ShowDialog(
            val inputString: String,
            val dialogState: LibTypeDialog,
            val inputListItems: List<LibGenItem>? = null
        ) : Label

        data object ShowSaveFilePicker : Label
        data object ShowLoadFilePicker : Label
    }

    sealed interface Action {
        data object StartGameStateFlow : Action
        data object StartGamePopupFlow : Action
        data object StartGameElementVisFlow : Action
        data object StartGameSettingsFlow : Action
        data object StartGameDialogFlow : Action
    }
}