package com.pixnpunk.dialogs.presentation

import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.ComponentContext
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.OutputState
import com.pixnpunk.settings.SettingsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RealDialogsComponent(
    private val componentContext: ComponentContext,
    override val dialogsConfig: DialogsConfig,
    override val addNewEntry: (GameEntry) -> Unit,
    override val editEntry: (GameEntry) -> Unit,
    override val downloadNewEntry: (GameEntry) -> Unit,
    override val selectFolder: () -> Unit,
    override val selectFile: () -> Unit,
    override val deselectFile: () -> Unit,
    override val selectColor: (Pair<ColorPickerState, Color>) -> Unit,
    override val confirmMessage: (OutputState) -> Unit,
    override val inputValue: (Pair<InputState, String>) -> Unit,
    override val doPlayGame: (GameEntry, Int) -> Unit,
    override val onCleanError: () -> Unit,
    override val onEnterValue: (Pair<String, Boolean>) -> Unit,
    override val onExecValue: (String) -> Unit,
    override val onSelMenuItem: (Int) -> Unit,
    override val onDismissed: () -> Unit,
) : ComponentContext by componentContext, DialogsComponent, KoinComponent {
    private val settingsRepo: SettingsRepository by inject()

    override val settingsFlow = settingsRepo.settingsFlow
}