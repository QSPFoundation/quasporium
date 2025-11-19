package com.pixnpunk.game.presentation

import android.net.Uri
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.pixnpunk.dialogs.presentation.DialogsComponent
import com.pixnpunk.dialogs.presentation.DialogsState
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.LibTypeDialog
import com.pixnpunk.extra.presentation.ExtraComponent
import com.pixnpunk.game.presentation.mvi.GameStore
import com.pixnpunk.main.presentation.MainComponent
import com.pixnpunk.`object`.presentation.ObjectComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface GameComponent {
    val childStack: Value<ChildStack<*, Child>>
    val dialogSlot: Value<ChildSlot<*, DialogsComponent>>
    val model: StateFlow<GameStore.State>
    val label: Flow<GameStore.Label>

    fun navigation(child: RealGameComponent.ChildConfig)

    fun runGame(
        gameId: Long,
        gameTitle: String,
        gameDirUri: Uri,
        gameFileUri: Uri
    )

    fun doShowDialog()
    fun doShowDialog(
        inputString: String,
        dialogState: LibTypeDialog,
        inputListItems: List<LibGenItem>? = null
    )

    fun doCreateSaveIntent()
    fun doCreateLoadIntent()
    fun restartGame()

    sealed class Child {
        class MainChild(val component: MainComponent) : Child()
        class ExtraChild(val component: ExtraComponent) : Child()
        class ObjectChild(val component: ObjectComponent) : Child()
    }
}