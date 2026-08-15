package com.pixnpunk.dialogs.presentation

import androidx.compose.runtime.Immutable
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.LibSaveItem
import com.pixnpunk.dto.OutputState
import com.pixnpunk.game.domain.model.InteractableItem
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class DialogsConfig(
    val dialogsState: DialogsState,
    val outputValue: String = "",
    val outputState: OutputState? = null,
    val inputState: InputState? = null,
    val colorPickerState: ColorPickerState? = null,
    val unfilledEntry: GameEntry = GameEntry(),
    val isError: Boolean = false,
    val initialValue: String = "",
    val supportValue: String = "",
    val supportErrorValue: String = "",
    val isScanningFolder: Boolean = false,
    val scannedFiles: List<String> = emptyList(),
    val selectedStartupFile: String? = null,
    val menuItems: List<InteractableItem> = emptyList(),
    val saveItems: List<LibSaveItem> = emptyList()
)
