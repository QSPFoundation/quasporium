package com.pixnpunk.game.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.child
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStop
import com.pixnpunk.`object`.presentation.RealObjectComponent
import com.pixnpunk.dialogs.presentation.DialogsComponent
import com.pixnpunk.dialogs.presentation.DialogsConfig
import com.pixnpunk.dialogs.presentation.DialogsState
import com.pixnpunk.dialogs.presentation.RealDialogsComponent
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.LibSaveItem
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.dto.OutputState
import com.pixnpunk.dto.OutputStateValue
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.extra.presentation.RealExtraComponent
import com.pixnpunk.game.domain.model.EngineDialog
import com.pixnpunk.game.domain.model.GameId
import com.pixnpunk.game.domain.model.GameState
import com.pixnpunk.game.domain.model.InteractableItem
import com.pixnpunk.game.domain.model.LibTypeDialog
import com.pixnpunk.game.domain.model.VfsPath
import com.pixnpunk.game.domain.model.WindowType
import com.pixnpunk.game.domain.repository.AudioPlayerInteractor
import com.pixnpunk.game.domain.repository.EngineCommand
import com.pixnpunk.game.domain.repository.GameEngineInteractor
import com.pixnpunk.game.domain.repository.SaveGameInteractor
import com.pixnpunk.game.presentation.model.BadgesHashes
import com.pixnpunk.game.presentation.model.BadgesState
import com.pixnpunk.game.presentation.model.GameUIState
import com.pixnpunk.main.presentation.MainComponent
import com.pixnpunk.main.presentation.RealMainComponent
import com.pixnpunk.settings.domain.repository.SettingsRepository
import com.pixnpunk.storage.DirUtil.isGameDirIsInternal
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class RealGameComponent(
    private val componentContext: ComponentContext,
    private val libAuthors: NativeLibAuthors,
    private val gameId: Long = -1L,
    private val gameTitle: String = "Title",
    private val gameDirUriBox: String = "",
    private val gameFileUriBox: String = "",
    private val gameSaveUriBox: String = ""
) : ComponentContext by componentContext, GameComponent, KoinComponent {

    private val componentScope = coroutineScope()

    internal val audioPlayer: AudioPlayerInteractor = get()
    private val engineInteractor: GameEngineInteractor by inject { parametersOf(libAuthors) }
    private val settingsRepository: SettingsRepository = get { parametersOf(gameId) }
    private val saveGameInteractor: SaveGameInteractor by inject { parametersOf(engineInteractor) }

    override val settingsState: StateFlow<SettingsEntity> = settingsRepository.settingsFlow
    override val engineState: StateFlow<GameState> = engineInteractor.state

    private val _uiState = MutableStateFlow(
        GameUIState(
            gameId = GameId(gameId),
            gameTitle = gameTitle,
            gameDirUriBox = VfsPath(gameDirUriBox),
            gameFileUriBox = VfsPath(gameFileUriBox)
        )
    )
    override val model: StateFlow<GameUIState> = _uiState.asStateFlow()

    private val lastHashes = MutableStateFlow(BadgesHashes())
    override val badgesState: StateFlow<BadgesState> = combine(
        engineState.filterIsInstance<GameState.Running>(),
        lastHashes
    ) { engine, hashes ->
        BadgesState(
            hasNewMain = engine.mainDescription.hashCode() != hashes.mainHash,
            hasNewActions = engine.actions.hashCode() != hashes.actionsHash,
            hasNewExtra = engine.varsDescription.hashCode() != hashes.extraHash,
            hasNewObjects = engine.objects.hashCode() != hashes.objectsHash
        )
    }.stateIn(
        componentScope,
        SharingStarted.Eagerly,
        BadgesState()
    )

    private val _events = Channel<GameEvent>(Channel.BUFFERED)
    override val events: Flow<GameEvent> = _events.receiveAsFlow()

    private val isRestoredAfterProcessDeath =
        stateKeeper.consume(KEY_SESSION, Boolean.serializer()) == true

    private val dialogNavigation = SlotNavigation<DialogsConfig>()
    private val extraNavigation = SlotNavigation<Unit>()
    private val objectNavigation = SlotNavigation<Unit>()

    override val dialogSlot: Value<ChildSlot<*, DialogsComponent>> =
        childSlot(
            source = dialogNavigation,
            serializer = null
        ) { config, context ->
            var isAnswered = false
            RealDialogsComponent(
                componentContext = context,
                settingsEntity = settingsState.value,
                dialogsConfig = config,
                onCleanError = ::onCleanError,
                inputValue = {
                    isAnswered = true
                    onInputValue(it)
                },
                confirmMessage = {
                    isAnswered = true
                    onConfirmMessage(it)
                },
                onDismissed = {
                    if (!isAnswered) {
                        engineInteractor.submitCommand(EngineCommand.DismissDialog)
                    }
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
                engineInteractor = engineInteractor,
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
                engineInteractor = engineInteractor,
                settingsState = settingsState
            )
        }

    override val mainComponent: MainComponent = RealMainComponent(
        componentContext = childContext("MainComponent"),
        engineInteractor = engineInteractor,
        settingsState = settingsState,
        gameDirUriBox = gameDirUriBox,
        onExecValue = ::onExecValue
    )

    private companion object {
        const val KEY_SESSION = "GameSession"
    }

    init {
        stateKeeper.register(KEY_SESSION, Boolean.serializer()) { true }

        lifecycle.doOnCreate {
            if (engineState.value is GameState.Idle) {
                engineInteractor.submitCommand(
                    EngineCommand.LoadGame(
                        VfsPath(gameFileUriBox),
                        VfsPath(gameDirUriBox)
                    )
                )
                componentScope.launch {
                    val autosave = if (isRestoredAfterProcessDeath) {
                        saveGameInteractor.findAutosave(_uiState.value.gameFileUriBox)
                    } else {
                        null
                    }
                    val save = autosave ?: gameSaveUriBox.takeIf { it.isNotBlank() }?.let(::VfsPath)
                    if (save != null) {
                        engineInteractor.submitCommand(EngineCommand.LoadSave(save))
                    }
                }
            }
        }

        lifecycle.doOnStop {
            if (_uiState.value.isFinishRequested) return@doOnStop
            val running = engineState.value as? GameState.Running ?: return@doOnStop
            if (running.uiConfig.isNoSave) return@doOnStop

            componentScope.launch { saveGameInteractor.autosave(VfsPath(gameFileUriBox)) }
        }

        lifecycle.doOnDestroy {
            engineInteractor.shutdown()
            settingsRepository.cancel()
        }

        settingsState
            .onEach { settings ->
                _uiState.update {
                    it.copy(
                        isExecutorEnabled = settings.isUseExecString
                    )
                }
            }
            .launchIn(componentScope)

        engineState
            .filterIsInstance<GameState.Running>()
            .onEach { state ->
                _uiState.update { current ->
                    current.copy(
                        isSaveDisable = state.uiConfig.isNoSave,
                        isObjectsEnabled = state.activeWindows.contains(WindowType.OBJS),
                        isExtraEnabled = state.activeWindows.contains(WindowType.VARS),
                        isInputEnabled = state.activeWindows.contains(WindowType.INPUT)
                    )
                }
            }
            .launchIn(componentScope)

        engineState
            .map { it.activeDialog }
            .distinctUntilChanged()
            .onEach { dialog ->
                if (dialog == null) {
                    dialogNavigation.dismiss()
                } else {
                    when (dialog) {
                        is EngineDialog.Message ->
                            doShowDialog(
                                LibTypeDialog.DIALOG_MESSAGE,
                                dialog.text
                            )

                        is EngineDialog.Input ->
                            doShowDialog(
                                LibTypeDialog.DIALOG_INPUT_BOX,
                                dialog.prompt
                            )

                        is EngineDialog.Picture ->
                            doShowDialog(
                                LibTypeDialog.DIALOG_PICTURE,
                                dialog.imagePath.value
                            )

                        is EngineDialog.Error ->
                            doShowDialog(
                                LibTypeDialog.DIALOG_ERROR,
                                dialog.details
                            )

                        is EngineDialog.Menu ->
                            doShowDialog(
                                LibTypeDialog.DIALOG_MENU,
                                inputListItems = dialog.items
                            )
                    }
                }
            }
            .launchIn(componentScope)
    }

    internal fun setGameVisible(isVisible: Boolean) = engineInteractor.setTimerPaused(!isVisible)

    override fun doSaveFile() {
        if (!isGameDirIsInternal(gameDirUriBox)) {
            _uiState.update { it.copy(isUserChooseSave = true) }
        } else {
            coroutineScope().launch {
                val savesCount =
                    saveGameInteractor.getSavesCount(gameId)
                doShowDialog(
                    inputString = "Save ${savesCount + 1}",
                    dialogState = LibTypeDialog.DIALOG_SAVEGAME
                )
            }
        }
    }

    override fun doLoadFile() {
        if (!isGameDirIsInternal(gameDirUriBox)) {
            _uiState.update { it.copy(isUserChooseLoad = true) }
        } else {
            coroutineScope().launch {
                saveGameInteractor.getAllSaves(gameId).let {
                    if (it.isEmpty()) {
                        _events.send(GameEvent.ShowSnackbar("No saved games!"))
                    } else {
                        doShowDialog(it)
                    }
                }
            }
        }
    }

    override fun onSaveFilePicked(path: VfsPath?) {
        _uiState.update { it.copy(isUserChooseSave = false) }
        if (path != null) {
            engineInteractor.submitCommand(EngineCommand.SaveGame(path))
        }
    }

    override fun onLoadFilePicked(path: VfsPath?) {
        _uiState.update { it.copy(isUserChooseLoad = false) }
        if (path != null) {
            engineInteractor.submitCommand(EngineCommand.LoadSave(path))
        }
    }

    private fun onConfirmMessage(value: OutputStateValue) {
        when (value.state) {
            OutputState.OUTPUT_RESTART_GAME ->
                engineInteractor.submitCommand(EngineCommand.RestartGame)

            OutputState.OUTPUT_LIST_MENU ->
                engineInteractor.submitCommand(EngineCommand.SelectMenuItem(value.menuIndex))

            OutputState.OUTPUT_LIST_SAVES ->
                engineInteractor.submitCommand(EngineCommand.LoadSave(VfsPath(value.selFileBox)))

            OutputState.OUTPUT_SIMPLE_MSG ->
                engineInteractor.submitCommand(EngineCommand.DismissDialog)

            else -> {}
        }
    }

    private fun onInputValue(msg: Pair<InputState, String>) {
        val (state, inputString) = msg
        when (state) {
            InputState.INPUT_WITH_MESSAGE_AREA ->
                engineInteractor.submitCommand(EngineCommand.SubmitInputArea(inputString))

            InputState.INPUT_WITH_MESSAGE_BOX ->
                engineInteractor.submitCommand(EngineCommand.SubmitInput(inputString))

            InputState.INPUT_SAVE_NAME ->
                componentScope.launch {
                    saveGameInteractor.createAndSaveSlot(
                        gameId,
                        inputString,
                        _uiState.value.gameDirUriBox
                    )

                    _events.send(GameEvent.ShowSnackbar("Game saved!"))
                }

            else -> engineInteractor.submitCommand(EngineCommand.ExecuteString(inputString))
        }
    }

    private fun onExecValue(inputString: String) =
        engineInteractor.submitCommand(EngineCommand.ExecuteString(inputString))

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
        val currentState = _uiState.value
        dialogNavigation.activate(
            when {
                currentState.isInputEnabled ->
                    DialogsConfig(
                        dialogsState = DialogsState.DIALOG_INPUT_VALUE,
                        inputState = InputState.INPUT_WITH_MESSAGE_AREA
                    )

                currentState.isExecutorEnabled ->
                    DialogsConfig(
                        dialogsState = DialogsState.DIALOG_INPUT_VALUE,
                        inputState = InputState.INPUT_WITH_MESSAGE_EXEC
                    )

                else -> return
            }
        )
    }

    override fun doShowDialog(
        listSaves: List<LibSaveItem>
    ) = dialogNavigation.activate(
        configuration = DialogsConfig(
            dialogsState = DialogsState.DIALOG_OUTPUT_VALUE,
            outputState = OutputState.OUTPUT_LIST_SAVES,
            saveItems = listSaves
        )
    )

    override fun doShowDialog(
        dialogState: LibTypeDialog,
        inputString: String,
        inputListItems: List<InteractableItem>
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

    override fun cleanMainUpdates() {
        val currentEngine = engineState.value as? GameState.Running ?: return
        lastHashes.update { it.copy(mainHash = currentEngine.mainDescription.hashCode()) }
    }

    override fun cleanActionsUpdates() {
        val currentEngine = engineState.value as? GameState.Running ?: return
        lastHashes.update { it.copy(actionsHash = currentEngine.actions.hashCode()) }
    }

    override fun cleanExtraUpdates() {
        val currentEngine = engineState.value as? GameState.Running ?: return
        lastHashes.update { it.copy(extraHash = currentEngine.varsDescription.hashCode()) }
    }

    override fun cleanObjectsUpdates() {
        val currentEngine = engineState.value as? GameState.Running ?: return
        lastHashes.update { it.copy(objectsHash = currentEngine.objects.hashCode()) }
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

    override fun doFinishGame() {
        engineInteractor.shutdown()
        componentScope.launch { saveGameInteractor.deleteAutosave(VfsPath(gameFileUriBox)) }
        _uiState.update { it.copy(isFinishRequested = true) }
    }
}
