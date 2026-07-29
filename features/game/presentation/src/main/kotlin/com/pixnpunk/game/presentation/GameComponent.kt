package com.pixnpunk.game.presentation

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.pixnpunk.dialogs.presentation.DialogsComponent
import com.pixnpunk.dto.LibSaveItem
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.extra.presentation.RealExtraComponent
import com.pixnpunk.game.domain.model.GameState
import com.pixnpunk.game.domain.model.InteractableItem
import com.pixnpunk.game.domain.model.LibTypeDialog
import com.pixnpunk.game.domain.model.VfsPath
import com.pixnpunk.game.presentation.model.BadgesState
import com.pixnpunk.game.presentation.model.GameUIState
import com.pixnpunk.main.presentation.MainComponent
import com.pixnpunk.`object`.presentation.RealObjectComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface GameComponent {
    val dialogSlot: Value<ChildSlot<*, DialogsComponent>>

    val mainComponent: MainComponent
    val extraSlot: Value<ChildSlot<*, RealExtraComponent>>
    val objectSlot: Value<ChildSlot<*, RealObjectComponent>>

    val model: StateFlow<GameUIState>
    val badgesState: StateFlow<BadgesState>
    val engineState: StateFlow<GameState>
    val events: Flow<GameEvent>

    val settingsState: StateFlow<SettingsEntity>

    fun doRestartGame()
    fun doFinishGame()

    fun doShowDialog()
    fun doShowDialog(listSaves: List<LibSaveItem>)
    fun doShowDialog(
        dialogState: LibTypeDialog,
        inputString: String = "",
        inputListItems: List<InteractableItem> = emptyList()
    )

    fun stateSwitchExtraSlot()
    fun stateSwitchObjectSlot()
    fun clearAllSlot()

    fun cleanMainUpdates()
    fun cleanActionsUpdates()
    fun cleanExtraUpdates()
    fun cleanObjectsUpdates()

    fun doSaveFile()
    fun doLoadFile()

    fun onSaveFilePicked(path: VfsPath?)
    fun onLoadFilePicked(path: VfsPath?)
}
