package com.pixnpunk.dialogs.presentation

import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.ComponentContext
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.OutputStateValue

class RealDialogsComponent(
    private val componentContext: ComponentContext,
    override val dialogsConfig: DialogsConfig,
    override val addNewEntry: (GameEntry) -> Unit = { },
    override val editEntry: (GameEntry) -> Unit = { },
    override val downloadNewEntry: (GameEntry) -> Unit = { },
    override val selectFolder: () -> Unit = { },
    override val deselectDir: () -> Unit = { },
    override val selectFiles: () -> Unit = { },
    override val selectFile: () -> Unit = { },
    override val deselectFile: (String) -> Unit = { },
    override val selectColor: (Pair<ColorPickerState, Color>) -> Unit = { },
    override val confirmMessage: (OutputStateValue) -> Unit = { },
    override val inputValue: (Pair<InputState, String>) -> Unit = { },
    override val doPlayGame: (GameEntry, Int) -> Unit = { _, _ -> },
    override val onCleanError: () -> Unit = { },
    override val onDismissed: () -> Unit = { },
) : ComponentContext by componentContext, DialogsComponent