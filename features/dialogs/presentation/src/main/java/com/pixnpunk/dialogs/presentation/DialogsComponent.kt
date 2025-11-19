package com.pixnpunk.dialogs.presentation

import androidx.compose.ui.graphics.Color
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.OutputState
import com.pixnpunk.dto.SettingsEntity
import kotlinx.coroutines.flow.StateFlow

sealed interface DialogsComponent {
    val dialogsConfig: DialogsConfig
    val addNewEntry: (GameEntry) -> Unit
    val editEntry: (GameEntry) -> Unit
    val downloadNewEntry: (GameEntry) -> Unit
    val selectFolder: () -> Unit
    val selectFile: () -> Unit
    val deselectFile: () -> Unit
    val selectColor: (Pair<ColorPickerState, Color>) -> Unit
    val confirmMessage: (OutputState) -> Unit
    val inputValue: (Pair<InputState, String>) -> Unit
    val doPlayGame: (GameEntry, Int) -> Unit
    val settingsFlow: StateFlow<SettingsEntity>
    val onCleanError: () -> Unit
    val onEnterValue: (Pair<String, Boolean>) -> Unit
    val onExecValue: (String) -> Unit
    val onSelMenuItem: (Int) -> Unit
    val onDismissed: () -> Unit
}