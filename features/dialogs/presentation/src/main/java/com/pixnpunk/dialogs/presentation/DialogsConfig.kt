package com.pixnpunk.dialogs.presentation

import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.OutputState
import kotlinx.serialization.Serializable

@Serializable
data class DialogsConfig(
    val dialogsState: DialogsState,
    val outputValue: String = "",
    val outputState: OutputState? = null,
    val inputState: InputState? = null,
    val colorPickerState: ColorPickerState? = null,
    val unfilledEntry: GameEntry = GameEntry(),
    val isError: Boolean = false,
    val supportValue: String = "",
    val supportErrorValue: String = "",
	val menuItems: List<LibGenItem> = emptyList()
)