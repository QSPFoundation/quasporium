package com.pixnpunk.quasporium

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.child
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.details.presentation.RealDetailsComponent
import com.pixnpunk.dialogs.presentation.DialogsComponent
import com.pixnpunk.dialogs.presentation.DialogsConfig
import com.pixnpunk.dialogs.presentation.DialogsState
import com.pixnpunk.dialogs.presentation.RealDialogsComponent
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.OutputState
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.quasporium.mvi.RealRootStore
import com.pixnpunk.quasporium.mvi.RootStore
import com.pixnpunk.settings.SettingsRepository
import com.pixnpunk.settings.presentation.RealSettingsComponent
import com.pixnpunk.stock.presentation.RealStockComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealRootComponent(
    private val componentContext: ComponentContext,
    private val launchDirPicker: () -> Unit,
    private val launchFilePicker: () -> Unit,
    private val launchFilesPicker: () -> Unit,
    private val launchStorageAccess: () -> Unit,
    private val launchEngineIntent: (Pair<GameEntry, Int>) -> Unit,
) : ComponentContext by componentContext, RootComponent, KoinComponent {

    private val dialogsNavigation = SlotNavigation<DialogsConfig>()
    private val detailsNavigation = SlotNavigation<DetailsConfig>()
    private val settingsNavigation = SlotNavigation<Unit>()

    private val store = instanceKeeper.getStore {
        RealRootStore(DefaultStoreFactory()).create()
    }

    override val settingsState: StateFlow<SettingsEntity> =
        get<SettingsRepository>().settingsFlow.stateIn(
            scope = CoroutineScope(Dispatchers.Main),
            started = SharingStarted.Eagerly,
            initialValue = get<SettingsRepository>().currentSettings
        )

    fun processPickedDir(dirName: String, dirPath: String) {
        val oldConfig = dialogSlot.child?.configuration as? DialogsConfig
        if (oldConfig != null && oldConfig.dialogsState == DialogsState.DIALOG_DOWNLOAD_GAME) {
            dialogsNavigation.activate(
                oldConfig.copy(
                    unfilledEntry = oldConfig.unfilledEntry.copy(
                        gameDirPath = dirPath
                    )
                )
            )
        } else {
            dialogsNavigation.activate(
                DialogsConfig(
                    dialogsState = DialogsState.DIALOG_ADD_GAME,
                    unfilledEntry = GameEntry(
                        title = dirName,
                        gameDirPath = dirPath,
                    )
                )
            )
        }
    }

    fun processPickedFile(filePath: String) {
        val oldConfig = dialogSlot.child?.configuration as? DialogsConfig
        if (oldConfig != null) {
            dialogsNavigation.activate(
                oldConfig.copy(
                    unfilledEntry = oldConfig.unfilledEntry.copy(
                        gameIconPath = filePath
                    )
                )
            )
        }
    }

    fun processPickedFiles(filePaths: List<String>) {
        val oldConfig = dialogSlot.child?.configuration as? DialogsConfig
        if (oldConfig != null) {
            val oldEntry = oldConfig.unfilledEntry
            val newFilesPath = oldEntry.gameFilesPath.plus(filePaths)
            dialogsNavigation.activate(
                oldConfig.copy(
                    unfilledEntry = oldEntry.copy(
                        gameFilesPath = newFilesPath
                    )
                )
            )
        }
    }

    private fun selGameDialog(entry: GameEntry) {
        when (entry.gameFilesPath.size) {
            0 -> {
                doShowDialog(
                    outputValue = "The game files for the ${entry.title} are missing!",
                    outputState = OutputState.OUTPUT_SIMPLE_MSG
                )
            }

            1 -> {
                launchEngineIntent(entry to 0)
            }

            else -> {
                dialogsNavigation.activate(
                    DialogsConfig(
                        dialogsState = DialogsState.DIALOG_SELECT_RUN_GAME,
                        unfilledEntry = entry
                    )
                )
            }
        }
    }

    override val dialogSlot: Value<ChildSlot<*, DialogsComponent>> =
        childSlot(
            source = dialogsNavigation,
            key = "DialogsChildSlot",
            serializer = serializer(),
        ) { config, ctx ->
            RealDialogsComponent(
                componentContext = ctx,
                dialogsConfig = config,
                addNewEntry = { store.accept(RootStore.Intent.AddNewGame(it)) },
                editEntry = { store.accept(RootStore.Intent.EditGame(it)) },
                downloadNewEntry = { entry ->
                    detailsNavigation.dismiss {
                        store.accept(RootStore.Intent.DownloadNewGame(entry))
                    }
                },
                selectFolder = launchDirPicker,
                deselectDir = ::remDirSelect,
                selectFile = launchFilePicker,
                selectFiles = launchFilesPicker,
                deselectFile = ::remFileSelect,
                selectColor = {
                    store.accept(RootStore.Intent.UpdateColorConfig(it.first, it.second))
                },
                confirmMessage = {
                    when (it.state) {
                        OutputState.OUTPUT_REQUEST_PERM -> launchStorageAccess()
                        else -> {}
                    }
                },
                inputValue = {
                    store.accept(RootStore.Intent.UpdateImageConfig(it.first, it.second))
                },
                doPlayGame = { entry, value -> launchEngineIntent(entry to value) },
                onDismissed = dialogsNavigation::dismiss,
            )
        }

    override val detailsSlot =
        childSlot(
            source = detailsNavigation,
            key = "DetailsChildStack",
            serializer = serializer(),
            handleBackButton = true
        ) { config, ctx ->
            RealDetailsComponent(
                componentContext = ctx,
                defEntry = config.entry,
                showSelectGameDialog = {
                    doShowDialog(
                        state = DialogsState.DIALOG_SELECT_RUN_GAME,
                        entry = it
                    )
                },
                showEditGameDialog = {
                    doShowDialog(
                        state = DialogsState.DIALOG_EDIT_GAME,
                        entry = it
                    )
                },
                showDownloadGameDialog = {
                    doShowDialog(
                        state = DialogsState.DIALOG_DOWNLOAD_GAME,
                        entry = it
                    )
                },
                onDismissed = { detailsNavigation.dismiss() },
            )
        }

    override val settingsSlot: Value<ChildSlot<Unit, RealSettingsComponent>> =
        childSlot(
            source = settingsNavigation,
            key = "SettingsChildStack",
            serializer = serializer(),
            handleBackButton = true
        ) { _, ctx ->
            RealSettingsComponent(
                componentContext = ctx,
                showColorDialog = ::doShowDialog,
                resetToDefault = { store.accept(RootStore.Intent.RestoreColorConfig(state = it)) },
                showInputDialog = ::doShowDialog,
                onDismissed = { settingsNavigation.dismiss() }
            )
        }

    override val stockComponent = RealStockComponent(
        componentContext = childContext(key = "StockComponent"),
        showDialogAdd = ::doShowDialog,
        navigateToSettings = {
            CoroutineScope(Dispatchers.Main).launch {
                settingsNavigation.activate(Unit)
            }
        },
        navigateToDetails = { detailsNavigation.activate(DetailsConfig(it)) }
    )

    private fun remDirSelect() {
        val oldConfig = dialogSlot.child?.configuration as? DialogsConfig
        if (oldConfig != null) {
            dialogsNavigation.activate(
                oldConfig.copy(
                    unfilledEntry = oldConfig.unfilledEntry.copy(
                        gameDirPath = ""
                    )
                )
            )
        }
    }

    private fun remFileSelect(path: String) {
        val oldConfig = dialogSlot.child?.configuration as? DialogsConfig
        if (oldConfig != null) {
            if (path.isNotEmpty()) {
                dialogsNavigation.activate(
                    oldConfig.copy(
                        unfilledEntry = oldConfig.unfilledEntry.copy(
                            gameFilesPath = let {
                                val list = oldConfig.unfilledEntry.gameFilesPath.toMutableList()
                                list.remove(path)
                                list
                            }
                        )
                    )
                )
            } else {
                dialogsNavigation.activate(
                    oldConfig.copy(
                        unfilledEntry = oldConfig.unfilledEntry.copy(
                            gameIconPath = ""
                        )
                    )
                )
            }
        }
    }

    override fun doShowDialog() =
        dialogsNavigation.activate(
            DialogsConfig(
                dialogsState = DialogsState.DIALOG_ADD_GAME
            )
        )

    override fun doShowDialog(
        state: DialogsState,
        entry: GameEntry
    ) {
        when (state) {
            DialogsState.DIALOG_SELECT_RUN_GAME -> selGameDialog(entry)

            DialogsState.DIALOG_DOWNLOAD_GAME -> {
                dialogsNavigation.activate(
                    DialogsConfig(
                        dialogsState = state,
                        unfilledEntry = entry
                    )
                )
            }

            DialogsState.DIALOG_EDIT_GAME -> {
                dialogsNavigation.activate(
                    DialogsConfig(
                        dialogsState = state,
                        unfilledEntry = entry
                    )
                )
            }

            else -> TODO("ERROR STATE!")
        }
    }

    override fun doShowDialog(
        pickerState: ColorPickerState
    ) =
        dialogsNavigation.activate(
            DialogsConfig(
                dialogsState = DialogsState.DIALOG_SELECT_COLOR,
                colorPickerState = pickerState
            )
        )

    override fun doShowDialog(
        inputState: InputState
    ) =
        dialogsNavigation.activate(
            DialogsConfig(
                dialogsState = DialogsState.DIALOG_INPUT_VALUE,
                inputState = inputState
            )
        )

    override fun doShowDialog(
        outputValue: String,
        outputState: OutputState
    ) =
        dialogsNavigation.activate(
            DialogsConfig(
                dialogsState = DialogsState.DIALOG_OUTPUT_VALUE,
                outputValue = outputValue,
                outputState = outputState
            )
        )

    @Serializable
    data class DetailsConfig(val entry: GameEntry)
}