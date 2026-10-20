package com.pixnpunk.quasporium

import androidx.compose.ui.graphics.toArgb
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
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.details.presentation.RealDetailsComponent
import com.pixnpunk.dialogs.presentation.DialogsComponent
import com.pixnpunk.dialogs.presentation.DialogsConfig
import com.pixnpunk.dialogs.presentation.DialogsState
import com.pixnpunk.dialogs.presentation.RealDialogsComponent
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.LaunchGameEntity
import com.pixnpunk.dto.OutputState
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.quasporium.mvi.RealRootStore
import com.pixnpunk.quasporium.mvi.RootStore
import com.pixnpunk.settings.domain.repository.SettingsRepository
import com.pixnpunk.settings.presentation.RealSettingsComponent
import com.pixnpunk.stock.presentation.RealStockComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class RealRootComponent(
    private val componentContext: ComponentContext,
    private val launchDirPicker: () -> Unit,
    private val launchFilePicker: () -> Unit,
    private val launchFontPicker: () -> Unit,
    private val launchStorageAccess: () -> Unit,
    private val launchEngineIntent: (LaunchGameEntity) -> Unit,
) : ComponentContext by componentContext, RootComponent, KoinComponent {

    private val dialogsNavigation = SlotNavigation<DialogsConfig>()
    private val detailsNavigation = SlotNavigation<DetailsConfig>()
    private val settingsNavigation = SlotNavigation<SettingsConfig>()

    private val settingsRepository: SettingsRepository by inject()

    override val settingsState: StateFlow<SettingsEntity> = settingsRepository.settingsFlow

    private val store =
        instanceKeeper.getStore {
            RealRootStore(
                storeFactory = DefaultStoreFactory(),
                executorFactory = { get() }
            ).create()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val model: StateFlow<RootStore.State> = store.stateFlow

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
        } else {
            detailsSlot.child?.instance?.onSetBackground(filePath)
        }
    }

    fun processPickedFont(fontPath: String) {
        val settings = settingsSlot.child?.instance
        if (settings != null && settings.isLocalSettings) {
            settings.updateCustomFontPath(fontPath)
            return
        }

        val oldCustomFontPath = settingsState.value.customFontPath

        store.accept(
            RootStore.Intent.UpdateCustomFontPath(
                path = fontPath,
                replacedPath = oldCustomFontPath.takeIf { it.isNotEmpty() && it != fontPath }
            )
        )
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
                settingsEntity = settingsState.value,
                addNewEntry = { store.accept(RootStore.Intent.AddNewGame(it)) },
                editEntry = { store.accept(RootStore.Intent.EditGame(it)) },
                downloadNewEntry = { entry ->
                    detailsNavigation.dismiss {
                        store.accept(RootStore.Intent.DownloadNewGame(entry))
                    }
                },
                selectFolder = launchDirPicker,
                deselectDir = ::remDirSelect,
                selectCover = launchFilePicker,
                deselectCover = ::remFileSelect,
                selectColor = { (state, color) ->
                    val settings = settingsSlot.child?.instance
                    if (settings != null) {
                        settings.updateColor(state, color.toArgb())
                    } else {
                        store.accept(RootStore.Intent.UpdateColorConfig(state, color))
                    }
                },
                confirmMessage = {
                    when (it.state) {
                        OutputState.OUTPUT_REQUEST_PERM -> launchStorageAccess()
                        else -> {}
                    }
                },
                inputValue = { (state, input) ->
                    val settings = settingsSlot.child?.instance
                    val value = input.toIntOrNull()
                    if (settings != null && value != null) {
                        settings.updateImageSize(state, value)
                    } else {
                        store.accept(RootStore.Intent.UpdateImageConfig(state, input))
                    }
                },
                onDismissed = dialogsNavigation::dismiss,
            )
        }

    override val detailsSlot =
        childSlot(
            source = detailsNavigation,
            key = "DetailsChildSlot",
            serializer = serializer(),
            handleBackButton = true
        ) { config, ctx ->
            RealDetailsComponent(
                componentContext = ctx,
                defEntry = config.entry,
                settingsState = settingsState,
                launchBackPicker = launchFilePicker,
                launchFreshGame = { launchEngineIntent(it) },
                launchContinueGame = { launchEngineIntent(it) },
                showEditGameDialog = {
                    doShowDialog(
                        state = DialogsState.DIALOG_EDIT_GAME,
                        entry = it
                    )
                },
                showSettingsGamePanel = {
                    settingsNavigation.activate(
                        SettingsConfig(
                            it.id,
                            it.title
                        )
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

    override val settingsSlot =
        childSlot(
            source = settingsNavigation,
            key = "SettingsChildSlot",
            serializer = serializer(),
            handleBackButton = true
        ) { config, ctx ->
            RealSettingsComponent(
                componentContext = ctx,
                gameId = config.gameId,
                gameTitle = config.gameTitle,
                showColorDialog = ::doShowDialog,
                showInputDialog = ::doShowDialog,
                showFontDialog = launchFontPicker,
                onDismissed = { settingsNavigation.dismiss() }
            )
        }

    override val stockComponent = RealStockComponent(
        componentContext = childContext(key = "StockComponent"),
        showDialogAdd = ::doShowDialog,
        navigateToSettings = {
            settingsNavigation.activate(
                SettingsConfig(null, "")
            )
        },
        navigateToDetails = {
            detailsNavigation.activate(DetailsConfig(it))
        },
        settingsState = settingsState
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

    private fun remFileSelect() {
        val oldConfig = dialogSlot.child?.configuration as? DialogsConfig
        if (oldConfig != null) {
            dialogsNavigation.activate(
                oldConfig.copy(
                    unfilledEntry = oldConfig.unfilledEntry.copy(
                        gameIconPath = ""
                    )
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
    data class SettingsConfig(val gameId: Long?, val gameTitle: String)

    @Serializable
    data class DetailsConfig(val entry: GameEntry)
}
