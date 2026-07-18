package com.pixnpunk.game.presentation

import android.net.Uri
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.child
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dialogs.presentation.DialogsComponent
import com.pixnpunk.dialogs.presentation.DialogsConfig
import com.pixnpunk.dialogs.presentation.DialogsState
import com.pixnpunk.dialogs.presentation.RealDialogsComponent
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.LibSaveItem
import com.pixnpunk.dto.OutputState
import com.pixnpunk.dto.OutputStateValue
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.extra.presentation.RealExtraComponent
import com.pixnpunk.game.data.GameNativeEngine
import com.pixnpunk.game.domain.model.LibTypeDialog
import com.pixnpunk.game.domain.repository.AudioPlayer
import com.pixnpunk.game.presentation.mvi.GameStore
import com.pixnpunk.game.presentation.mvi.RealGameStore
import com.pixnpunk.main.presentation.MainComponent
import com.pixnpunk.main.presentation.RealMainComponent
import com.pixnpunk.`object`.presentation.RealObjectComponent
import com.pixnpunk.settings.data.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class RealGameComponent(
    private val componentContext: ComponentContext,
    private val onFinishActivity: () -> Unit,
    private val gameId: Long = -1L,
    private val gameTitle: String = "Title",
    private val gameDirUriBox: String = "",
    private val gameFileUriBox: String = ""
) : ComponentContext by componentContext, GameComponent, KoinComponent {

    private val componentScope = coroutineScope()

    internal val audioPlayer: AudioPlayer = get()
    private val engine = instanceKeeper.getOrCreate("NativeGameEngine") { get<GameNativeEngine>() }

    private val dialogNavigation = SlotNavigation<DialogsConfig>()
    private val extraNavigation = SlotNavigation<Unit>()
    private val objectNavigation = SlotNavigation<Unit>()

    private val settingsRepository: SettingsRepository =
        instanceKeeper.getOrCreate {
            get<SettingsRepository> { parametersOf(gameId) }
        }

    override val settingsState: StateFlow<SettingsEntity> =
        settingsRepository.settingsFlow.stateIn(
            scope = componentScope,
            started = SharingStarted.Eagerly,
            initialValue = settingsRepository.currentSettings
        )

    private val store =
        instanceKeeper.getStore {
            RealGameStore(
                gameId = gameId,
                gameTitle = gameTitle,
                gameDirUriBox = gameDirUriBox,
                gameFileUriBox = gameFileUriBox,
                storeFactory = DefaultStoreFactory(),
                gameNativeEngine = engine,
                audioPlayer = audioPlayer,
                settingsState = settingsState
            ).create()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<GameStore.State> = store.stateFlow
    override val label = store.labels

    override val dialogSlot: Value<ChildSlot<*, DialogsComponent>> =
        childSlot(
            source = dialogNavigation,
            serializer = DialogsConfig.serializer()
        ) { config, context ->
            RealDialogsComponent(
                componentContext = context,
                dialogsConfig = config,
                onCleanError = ::onCleanError,
                inputValue = ::onInputValue,
                confirmMessage = ::onConfirmMessage,
                onDismissed = {
                    store.accept(GameStore.Intent.OnDismissDialog)
                    dialogNavigation.dismiss()
                }
            )
        }

    override val extraSlot: Value<ChildSlot<*, RealExtraComponent>> =
        childSlot(
            key = "ExtraChildStack",
            source = extraNavigation,
            serializer = serializer(),
            handleBackButton = true
        ) { _, ctx ->
            RealExtraComponent(
                componentContext = ctx,
                gameNativeEngine = engine,
                settingsState = settingsState,
                gameDirUriBox = gameDirUriBox,
                onExecValue = ::onExecValue
            )
        }

    override val objectSlot: Value<ChildSlot<*, RealObjectComponent>> =
        childSlot(
            key = "ObjectChildStack",
            source = objectNavigation,
            serializer = serializer(),
            handleBackButton = true
        ) { _, ctx ->
            RealObjectComponent(
                componentContext = ctx,
                gameNativeEngine = engine,
                settingsState = settingsState
            )
        }

    override val mainComponent: MainComponent = RealMainComponent(
        componentContext = childContext("MainComponent"),
        gameNativeEngine = engine,
        settingsState = settingsState,
        gameDirUriBox = gameDirUriBox,
        onExecValue = ::onExecValue
    )

    fun onSaveFileResult(fileUri: Uri) {
        store.accept(GameStore.Intent.OnSaveFile(fileUri))
    }

    fun onLoadFileResult(fileUri: Uri) {
        store.accept(GameStore.Intent.OnLoadFile(fileUri))
    }

    fun onFileCorruptResult(isCancelUser: Boolean = false) {
        store.accept(GameStore.Intent.OnCorruptFile(isCancelUser))
    }

    override fun doSaveFile() = store.accept(GameStore.Intent.StartSaveFile)

    override fun doLoadFile() = store.accept(GameStore.Intent.StartLoadFile)

    private fun onConfirmMessage(value: OutputStateValue) {
        when (value.state) {
            OutputState.OUTPUT_RESTART_GAME -> engine.onRestartGame()

            OutputState.OUTPUT_LIST_MENU ->
                store.accept(GameStore.Intent.OnSelectMenuItem(value.menuIndex))

            OutputState.OUTPUT_LIST_SAVES ->
                store.accept(GameStore.Intent.OnLoadFile(value.selFileBox.toUri()))

            OutputState.OUTPUT_SIMPLE_MSG ->
                store.accept(GameStore.Intent.OnDismissDialog)

            else -> {}
        }
    }

    private fun onInputValue(msg: Pair<InputState, String>) {
        store.accept(
            GameStore.Intent.OnInputValue(msg.first, msg.second)
        )
    }

    private fun onExecValue(inputString: String) {
        store.accept(GameStore.Intent.OnExecCode(inputString))
    }

    private fun onCleanError() {
        val oldConfig = dialogSlot.child?.configuration as DialogsConfig
        dialogNavigation.activate(
            configuration = oldConfig.copy(
                isError = false,
                supportErrorValue = ""
            )
        )
    }

    override fun doShowDialog() {
        val currentState = store.state
        if (currentState.isInputEnabled) {
            doShowDialog(
                dialogState = LibTypeDialog.DIALOG_INPUT_AREA
            )
        } else if (currentState.isExecutorEnabled) {
            doShowDialog(
                dialogState = LibTypeDialog.DIALOG_INPUT_EXEC
            )
        }
    }

    override fun doShowDialog(listSaves: List<LibSaveItem>) {
        dialogNavigation.activate(
            configuration = DialogsConfig(
                dialogsState = DialogsState.DIALOG_OUTPUT_VALUE,
                outputState = OutputState.OUTPUT_LIST_SAVES,
                saveItems = listSaves
            )
        )
    }

    override fun doShowDialog(
        dialogState: LibTypeDialog,
        inputString: String,
        inputListItems: List<LibGenItem>
    ) {
        when (dialogState) {
            LibTypeDialog.DIALOG_SAVEGAME -> {
                dialogNavigation.activate(
                    DialogsConfig(
                        dialogsState = DialogsState.DIALOG_INPUT_VALUE,
                        inputState = InputState.INPUT_SAVE_NAME,
                        initialValue = inputString
                    )
                )
            }

            LibTypeDialog.DIALOG_MESSAGE -> {
                val oldConfig = dialogSlot.child?.configuration as? DialogsConfig
                if (oldConfig != null && oldConfig.dialogsState == DialogsState.DIALOG_OUTPUT_VALUE) {
                    dialogNavigation.activate(
                        configuration = oldConfig.copy(
                            isError = true,
                            supportErrorValue = inputString
                        )
                    )
                } else {
                    dialogNavigation.activate(
                        DialogsConfig(
                            dialogsState = DialogsState.DIALOG_OUTPUT_VALUE,
                            outputValue = inputString,
                            outputState = OutputState.OUTPUT_SIMPLE_MSG
                        )
                    )
                }
            }

            LibTypeDialog.DIALOG_ERROR -> {
                dialogNavigation.activate(
                    configuration = DialogsConfig(
                        dialogsState = DialogsState.DIALOG_OUTPUT_VALUE,
                        outputState = OutputState.OUTPUT_SIMPLE_MSG,
                        outputValue = inputString
                    )
                )
            }

            LibTypeDialog.DIALOG_INPUT_BOX,
            LibTypeDialog.DIALOG_INPUT_AREA,
            LibTypeDialog.DIALOG_INPUT_EXEC ->
                dialogNavigation.activate(
                    configuration = DialogsConfig(
                        dialogsState = DialogsState.DIALOG_INPUT_VALUE,
                        inputState =
                            when (dialogState) {
                                LibTypeDialog.DIALOG_INPUT_BOX -> InputState.INPUT_WITH_MESSAGE_BOX
                                LibTypeDialog.DIALOG_INPUT_AREA -> InputState.INPUT_WITH_MESSAGE_AREA
                                else -> InputState.INPUT_WITH_MESSAGE_EXEC
                            },
                        supportValue = inputString
                    )
                )

            LibTypeDialog.DIALOG_PICTURE -> {
                dialogNavigation.activate(
                    configuration = DialogsConfig(
                        dialogsState = DialogsState.DIALOG_OUTPUT_VALUE,
                        outputState = OutputState.OUTPUT_IMAGE,
                        outputValue = inputString
                    )
                )
            }

            LibTypeDialog.DIALOG_MENU ->
                dialogNavigation.activate(
                    configuration = DialogsConfig(
                        dialogsState = DialogsState.DIALOG_OUTPUT_VALUE,
                        outputState = OutputState.OUTPUT_LIST_MENU,
                        menuItems = inputListItems
                    )
                )
        }
    }

    override fun stateSwitchExtraSlot() {
        extraNavigation.dismiss()
        objectNavigation.dismiss()
        extraNavigation.activate(Unit)
    }

    override fun stateSwitchObjectSlot() {
        extraNavigation.dismiss()
        objectNavigation.dismiss()
        objectNavigation.activate(Unit)
    }

    override fun clearAllSlot() {
        extraNavigation.dismiss()
        objectNavigation.dismiss()
    }

    override fun cleanMainUpdates() =
        store.accept(GameStore.Intent.CleanMainSavedDesc)

    override fun cleanActionsUpdates() =
        store.accept(GameStore.Intent.CleanActionsSavedList)

    override fun cleanExtraUpdates() =
        store.accept(GameStore.Intent.CleanExtraSavedDesc)

    override fun cleanObjectsUpdates() =
        store.accept(GameStore.Intent.CleanObjectsSavedList)

    override fun doRunGame() {
        val currState = store.state
        if (!currState.isGameRunning) {
            engine.startService(
                settingsRepository,
                currState.gameId,
                currState.gameTitle,
                currState.gameDirUriBox,
                currState.gameFileUriBox
            )
        }
    }

    override fun doRestartGame() {
        dialogNavigation.activate(
            configuration = DialogsConfig(
                dialogsState = DialogsState.DIALOG_OUTPUT_VALUE,
                outputState = OutputState.OUTPUT_RESTART_GAME,
                outputValue = "Restart game? All unsaved progress might be lost"
            )
        )
    }

    override fun doFinishGame() = onFinishActivity()
}