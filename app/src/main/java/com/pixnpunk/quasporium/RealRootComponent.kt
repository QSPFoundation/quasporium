package com.pixnpunk.quasporium

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.panels.Panels
import com.arkivanov.decompose.router.panels.PanelsNavigation
import com.arkivanov.decompose.router.panels.activateDetails
import com.arkivanov.decompose.router.panels.activateExtra
import com.arkivanov.decompose.router.panels.childPanels
import com.arkivanov.decompose.router.panels.dismissDetails
import com.arkivanov.decompose.router.panels.dismissExtra
import com.arkivanov.decompose.router.panels.setMode
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
import com.pixnpunk.details.presentation.DetailsComponent
import com.pixnpunk.details.presentation.RealDetailsComponent
import com.pixnpunk.dialogs.presentation.DialogsComponent
import com.pixnpunk.dialogs.presentation.DialogsConfig
import com.pixnpunk.dialogs.presentation.DialogsState
import com.pixnpunk.dialogs.presentation.RealDialogsComponent
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.GameListType
import com.pixnpunk.dto.OutputState
import com.pixnpunk.quasporium.mvi.RealRootStore
import com.pixnpunk.quasporium.mvi.RootStore
import com.pixnpunk.settings.presentation.RealSettingsComponent
import com.pixnpunk.settings.presentation.SettingsComponent
import com.pixnpunk.stock.domain.usecase.FetchRemoteDescUseCase
import com.pixnpunk.stock.presentation.RealStockComponent
import com.pixnpunk.stock.presentation.StockComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
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

    @OptIn(ExperimentalDecomposeApi::class)
    private val panelNavigation = PanelsNavigation<Unit, DetailsConfig, Unit>()
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

    fun showMessageDialog(
        message: String,
        outputState: OutputState
    ) {
        dialogsNavigation.activate(
            DialogsConfig(
                dialogsState = DialogsState.DIALOG_OUTPUT_VALUE,
                outputValue = message,
                outputState = outputState
            )
        )
    }

    private fun selGameDialog(entry: GameEntry) {
        when (entry.gameFilesPath.size) {
            0 -> {
                showMessageDialog(
                    message = "The game files for the ${entry.title} are missing!",
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
                deselectDir = this::remDirSelect,
                selectFile = launchFilePicker,
                selectFiles = launchFilesPicker,
                deselectFile = this::remFileSelect,
                selectColor = {
                    store.accept(RootStore.Intent.UpdateColorConfig(it.first, it.second))
                },
                confirmMessage = {
                    when (it) {
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
                onCleanError = { },
                onEnterValue = { },
                onExecValue = { },
                onSelMenuItem = { },
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

    @OptIn(ExperimentalDecomposeApi::class, ExperimentalSerializationApi::class)
    override val childPanel: Value<ChildPanels<*, StockComponent, *, DetailsComponent, *, SettingsComponent>> =
        childPanels(
            source = panelNavigation,
            serializers = Triple(
                Unit.serializer(),
                DetailsConfig.serializer(),
                Unit.serializer()
            ),
            initialPanels = { Panels(main = Unit) },
            handleBackButton = true,
            extraFactory = { _, ctx ->
                RealSettingsComponent(
                    componentContext = ctx,
                    showColorDialog = {
                        dialogsNavigation.activate(
                            DialogsConfig(
                                dialogsState = DialogsState.DIALOG_SELECT_COLOR,
                                colorPickerState = it
                            )
                        )
                    },
                    showInputDialog = {
                        dialogsNavigation.activate(
                            DialogsConfig(
                                dialogsState = DialogsState.DIALOG_INPUT_VALUE,
                                inputState = it
                            )
                        )
                    },
                    onDismissed = panelNavigation::dismissExtra
                )
            },
            mainFactory = { _, ctx ->
                RealStockComponent(
                    componentContext = ctx,
                    showDialogAdd = {
                        dialogsNavigation.activate(
                            DialogsConfig(
                                dialogsState = DialogsState.DIALOG_ADD_GAME
                            )
                        )
                    },
                    navigateToDetails = { entryShow ->
                        if (entryShow.listId == GameListType.Playable) {
                            panelNavigation.activateDetails(details = DetailsConfig(entryShow))
                        } else {
                            CoroutineScope(Dispatchers.IO).launch {
                                val desc = get<FetchRemoteDescUseCase>().execute(entryShow.descUrl)

                                withContext(Dispatchers.Main) {
                                    panelNavigation.activateDetails(details = DetailsConfig(entryShow.copy(descUrl = desc)))
                                }
                            }
                        }
                    },
                    navigateToSettings = {
                        panelNavigation.activateExtra(Unit)
                    }
                )
            },
            detailsFactory = { cfg, ctx ->
                RealDetailsComponent(
                    componentContext = ctx,
                    entryShow = cfg.entryShow,
                    showSelectGameDialog = { selGameDialog(it) },
                    showEditGameDialog = {
                        dialogsNavigation.activate(
                            DialogsConfig(
                                dialogsState = DialogsState.DIALOG_EDIT_GAME,
                                unfilledEntry = it
                            )
                        )
                    },
                    showDownloadGameDialog = {
                        dialogsNavigation.activate(
                            DialogsConfig(
                                dialogsState = DialogsState.DIALOG_DOWNLOAD_GAME,
                                unfilledEntry = it
                            )
                        )
                    },
                    onDismissed = panelNavigation::dismissDetails
                )
            }
        )

    @OptIn(ExperimentalDecomposeApi::class)
    override fun setMode(mode: ChildPanelsMode) {
        panelNavigation.setMode(mode)
    }

    @Serializable
    private data class DetailsConfig(val entryShow: GameEntry)
}