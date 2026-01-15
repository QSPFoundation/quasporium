package com.pixnpunk.game.presentation

import android.net.Uri
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.pixnpunk.dialogs.presentation.DialogsComponent
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.LibTypeDialog
import com.pixnpunk.extra.presentation.ExtraComponent
import com.pixnpunk.game.presentation.mvi.GameStore
import com.pixnpunk.main.presentation.MainComponent
import com.pixnpunk.`object`.presentation.ObjectComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface GameComponent {
    val childStandardStack: Value<ChildStack<*, StandardChild>>
    val childExpandStack: Value<ChildStack<*, ExpandChild>>

    val dialogSlot: Value<ChildSlot<*, DialogsComponent>>
    val model: StateFlow<GameStore.State>
    val label: Flow<GameStore.Label>

    fun standardNavigation(child: RealGameComponent.StandardChildConfig)
    fun expandNavigation(child: RealGameComponent.ExpandChildConfig)

    fun runGame(
        gameId: Long,
        gameTitle: String,
        gameDirUri: Uri,
        gameFileUri: Uri
    )

    fun doShowDialog()
    fun doShowDialog(
        inputString: String = "",
        dialogState: LibTypeDialog,
        inputListItems: List<LibGenItem>? = null
    )

    fun doCreateSaveIntent()
    fun doCreateLoadIntent()
    fun restartGame()

    sealed class StandardChild {
        class MainChild(val component: MainComponent) : StandardChild()
        class ExtraChild(val component: ExtraComponent) : StandardChild()
        class ObjectChild(val component: ObjectComponent) : StandardChild()
    }

    sealed class ExpandChild {
        class MainActionsChild(val component: MainComponent) : ExpandChild()
        class ExtraChild(val component: ExtraComponent) : ExpandChild()
        class ObjectChild(val component: ObjectComponent) : ExpandChild()
    }
}