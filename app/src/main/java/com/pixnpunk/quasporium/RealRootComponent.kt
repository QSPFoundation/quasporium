package com.pixnpunk.quasporium

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.child
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dialogs.presentation.DialogsComponent
import com.pixnpunk.dialogs.presentation.DialogsConfig
import com.pixnpunk.dialogs.presentation.DialogsState
import com.pixnpunk.dialogs.presentation.RealDialogsComponent
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.OutputState
import com.pixnpunk.quasporium.mvi.RealRootStore
import com.pixnpunk.quasporium.mvi.RootStore
import com.pixnpunk.stock.domain.usecase.FetchRemoteDescUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.Locale

class RealRootComponent(
    private val componentContext: ComponentContext,
    private val launchDirPicker: () -> Unit,
    private val launchFilePicker: () -> Unit,
    private val launchFilesPicker: () -> Unit,
    private val launchStorageAccess: () -> Unit,
    private val launchEngineIntent: (Pair<GameEntry, Int>) -> Unit,
) : ComponentContext by componentContext, RootComponent, KoinComponent {

    private val dialogsNavigation = SlotNavigation<DialogsConfig>()

    private val store = instanceKeeper.getStore {
        RealRootStore(DefaultStoreFactory()).create()
    }

    override val label: Flow<RootStore.Label> = store.labels

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
            dialogsNavigation.activate(
                oldConfig.copy(
                    unfilledEntry = oldConfig.unfilledEntry.copy(
                        gameFilesPath = filePaths
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
                downloadNewEntry = { store.accept(RootStore.Intent.DownloadNewGame(it)) },
                selectFolder = launchDirPicker,
                deselectDir = ::remDirSelect,
                selectFile = launchFilePicker,
                selectFiles = launchFilesPicker,
                deselectFile = ::remFileSelect,
                selectColor = {
                    store.accept(RootStore.Intent.UpdateColorConfig(it.first, it.second))
                },
                confirmMessage = {
                    when (it.first) {
                        OutputState.OUTPUT_REQUEST_PERM -> launchStorageAccess()
                        OutputState.OUTPUT_SIMPLE_MSG -> {}
                        OutputState.OUTPUT_IMAGE -> {}
                        OutputState.OUTPUT_LIST -> {}
                    }
                },
                inputValue = {
                    store.accept(RootStore.Intent.UpdateImageConfig(it.first, it.second))
                },
                doPlayGame = { entry, value -> launchEngineIntent(entry to value) },
                onDismissed = dialogsNavigation::dismiss,
            )
        }

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

    override fun fetchDescConvData(
        entry: GameEntry,
        completeScope: (GameEntry) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            completeScope(
                entry.copy(
                    descUrl = get<FetchRemoteDescUseCase>().execute(entry.descUrl),
                    pubDate =
                        if (entry.pubDate.isNotEmpty())
                            PrettyTime().format(dataFormat.parse(entry.pubDate))
                        else entry.pubDate,
                    modDate =
                        if (entry.modDate.isNotEmpty())
                            PrettyTime().format(dataFormat.parse(entry.modDate))
                        else entry.modDate
                )
            )
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
}