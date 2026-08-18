package com.pixnpunk.dialogs.presentation

import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.ComponentContext
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.OutputStateValue
import com.pixnpunk.dto.SettingsEntity

class RealDialogsComponent(
    private val componentContext: ComponentContext,
    override val dialogsConfig: DialogsConfig,
    override val settingsEntity: SettingsEntity,
    override val addNewEntry: (GameEntry) -> Unit = { },
    override val editEntry: (GameEntry) -> Unit = { },
    override val downloadNewEntry: (GameEntry) -> Unit = { },
    override val selectFolder: () -> Unit = { },
    override val deselectDir: () -> Unit = { },
    override val selectCover: () -> Unit = { },
    override val deselectCover: () -> Unit = { },
    override val selectColor: (Pair<ColorPickerState, Color>) -> Unit = { },
    override val confirmMessage: (OutputStateValue) -> Unit = { },
    override val inputValue: (Pair<InputState, String>) -> Unit = { },
    override val onCleanError: () -> Unit = { },
    override val onDismissed: () -> Unit = { },
) : ComponentContext by componentContext, DialogsComponent
