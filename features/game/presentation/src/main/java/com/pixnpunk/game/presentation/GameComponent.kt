package com.pixnpunk.game.presentation

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.pixnpunk.dialogs.presentation.DialogsComponent
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.LibTypeDialog
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.extra.presentation.RealExtraComponent
import com.pixnpunk.game.presentation.mvi.GameStore
import com.pixnpunk.main.presentation.MainComponent
import com.pixnpunk.`object`.presentation.RealObjectComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface GameComponent {
    val dialogSlot: Value<ChildSlot<*, DialogsComponent>>

    val mainComponent: MainComponent
    val extraSlot: Value<ChildSlot<*, RealExtraComponent>>
    val objectSlot: Value<ChildSlot<*, RealObjectComponent>>

    val model: StateFlow<GameStore.State>
    val label: Flow<GameStore.Label>

    fun runGame(nativeLibFrom: NativeLibAuthors)
    fun restartGame()

    fun doShowDialog()
    fun doShowDialog(
        dialogState: LibTypeDialog,
        inputString: String = "",
        inputListItems: List<LibGenItem> = emptyList()
    )

    fun stateSwitchExtraSlot()
    fun stateSwitchObjectSlot()
    fun clearAllSlot()

    fun cleanCountUpdates()

    fun doSaveFile()
    fun doLoadFile()
}