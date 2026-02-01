package com.pixnpunk.quasporium

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.pixnpunk.dialogs.presentation.DialogsComponent
import com.pixnpunk.dialogs.presentation.DialogsState
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.OutputState
import com.pixnpunk.quasporium.mvi.RootStore
import kotlinx.coroutines.flow.Flow

sealed interface RootComponent {
    val dialogSlot: Value<ChildSlot<*, DialogsComponent>>

    val label: Flow<RootStore.Label>

    fun fetchDescConvData(
        entry: GameEntry,
        completeScope: (GameEntry) -> Unit
    )

    fun doShowDialog()
    fun doShowDialog(state: DialogsState, entry: GameEntry)
    fun doShowDialog(pickerState: ColorPickerState)
    fun doShowDialog(inputState: InputState)
    fun doShowDialog(outputValue: String, outputState: OutputState)
}