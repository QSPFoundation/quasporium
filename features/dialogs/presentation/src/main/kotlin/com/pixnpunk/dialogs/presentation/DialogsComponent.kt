package com.pixnpunk.dialogs.presentation

import androidx.compose.ui.graphics.Color
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.OutputStateValue

sealed interface DialogsComponent {
    val dialogsConfig: DialogsConfig
    val addNewEntry: (GameEntry) -> Unit
    val editEntry: (GameEntry) -> Unit
    val downloadNewEntry: (GameEntry) -> Unit
    val selectFolder: () -> Unit
    val deselectDir: () -> Unit
    val selectFiles: () -> Unit
    val selectCover: () -> Unit
    val deselectCover: (String) -> Unit
    val selectColor: (Pair<ColorPickerState, Color>) -> Unit
    val confirmMessage: (OutputStateValue) -> Unit
    val inputValue: (Pair<InputState, String>) -> Unit
    val onCleanError: () -> Unit
    val onDismissed: () -> Unit
}
