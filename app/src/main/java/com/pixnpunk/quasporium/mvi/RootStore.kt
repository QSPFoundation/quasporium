package com.pixnpunk.quasporium.mvi

import androidx.compose.ui.graphics.Color
import com.arkivanov.mvikotlin.core.store.Store
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.InputState
import kotlinx.serialization.Serializable

sealed interface RootStore : Store<RootStore.Intent, RootStore.State, RootStore.Label> {
    @Serializable
    data object State

    sealed interface Intent {
        data class AddNewGame(val entry: GameEntry) : Intent
        data class EditGame(val entry: GameEntry) : Intent
        data class DownloadNewGame(val entry: GameEntry) : Intent
        data class RestoreColorConfig(val state: ColorPickerState) : Intent
        data class UpdateColorConfig(val state: ColorPickerState, val color: Color) : Intent
        data class UpdateImageConfig(val state: InputState, val input: String) : Intent
        data class UpdateCustomFontPath(val path: String) : Intent
    }

    sealed interface Message

    sealed interface Label

    sealed interface Action
}