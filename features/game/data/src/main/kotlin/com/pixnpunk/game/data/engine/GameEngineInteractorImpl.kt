package com.pixnpunk.game.data.engine

import com.pixnpunk.game.data.engine.jni.NativeEngineBridge
import com.pixnpunk.game.data.engine.jni.NativeEngineListener
import com.pixnpunk.game.data.model.RawItem
import com.pixnpunk.game.data.vfs.FileSystemProvider
import com.pixnpunk.game.domain.model.EngineDialog
import com.pixnpunk.game.domain.model.GameId
import com.pixnpunk.game.domain.model.GameState
import com.pixnpunk.game.domain.model.InteractableItem
import com.pixnpunk.game.domain.model.LibUIConfig
import com.pixnpunk.game.domain.model.VfsPath
import com.pixnpunk.game.domain.model.WindowType
import com.pixnpunk.game.domain.repository.AudioPlayerInteractor
import com.pixnpunk.game.domain.repository.EngineCommand
import com.pixnpunk.game.domain.repository.GameEngineInteractor
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

internal class GameEngineInteractorImpl(
    private val bridge: NativeEngineBridge,
    private val vfsProvider: FileSystemProvider,
    private val audioService: AudioPlayerInteractor
) : GameEngineInteractor, NativeEngineListener {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val engineDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val engineScope = CoroutineScope(engineDispatcher + SupervisorJob())

    private val commandChannel = Channel<EngineCommand>(Channel.UNLIMITED)

    private val _state = MutableStateFlow<GameState>(GameState.Idle())
    override val state = _state.asStateFlow()

    private val isUiBlocked = atomic(false)
    private val counterIntervalMs = atomic(500L)

    private var pendingMessageDeferred: CompletableDeferred<Unit>? = null
    private var pendingInputDeferred: CompletableDeferred<String>? = null
    private var pendingMenuDeferred: CompletableDeferred<Int>? = null

    init {
        bridge.setListener(this)
        bridge.initializeEngine()

        startActorLoop()
        startGameLoopTimer()
    }

    override fun shutdown() {
        engineScope.cancel()
        bridge.destroyEngine()
    }

    private fun startActorLoop() = engineScope.launch {
        for (command in commandChannel) {
            processCommand(command)
        }
    }

    private fun startGameLoopTimer() = engineScope.launch {
        while (isActive) {
            val delayMs = counterIntervalMs.value
            if (delayMs <= 0) {
                delay(100.milliseconds)
            } else {
                delay(delayMs.milliseconds)
                if (_state.value is GameState.Running && !isUiBlocked.value) {
                    bridge.executeCounter()
                }
            }
        }
    }

    override fun onSetTimer(msecs: Int) {
        counterIntervalMs.value = msecs.toLong()
    }

    override fun submitCommand(command: EngineCommand) {
        when (command) {
            is EngineCommand.SubmitInput -> {
                pendingInputDeferred?.complete(command.text)
                pendingInputDeferred = null
                updateDialogState(null)
            }

            is EngineCommand.SelectMenuItem -> {
                pendingMenuDeferred?.complete(command.index)
                pendingMenuDeferred = null
                updateDialogState(null)
            }

            is EngineCommand.DismissDialog -> {
                pendingMessageDeferred?.complete(Unit)
                pendingMessageDeferred = null
                updateDialogState(null)
            }

            else -> commandChannel.trySend(command)
        }
    }

    private fun processCommand(command: EngineCommand) {
        when (command) {
            is EngineCommand.ExecuteAction -> bridge.executeAction(command.index)
            is EngineCommand.ExecuteString -> bridge.executeString(command.input)
            is EngineCommand.SelectObject -> bridge.selectObject(command.index)
            is EngineCommand.SubmitInputArea -> bridge.onInputAreaClicked(command.text)

            is EngineCommand.RestartGame -> {
                _state.value = GameState.Loading()
                audioService.closeAllFiles()

                engineScope.launch { bridge.restartGame() }
            }

            is EngineCommand.LoadGame -> {
                val file = vfsProvider.resolve(command.path) ?: return

                _state.value = GameState.Loading()
                audioService.closeAllFiles()
                vfsProvider.setGameRoot(file.path)

                engineScope.launch {
                    val isLoaded = bridge.loadGame(file, isGame = true)
                    if (isLoaded) {
                        bridge.restartGame()
                    } else {
                        _state.value = GameState.Idle()
                        onError("Failed to load game!")
                    }
                }
            }

            is EngineCommand.SaveGame -> {
                val saveFile = vfsProvider.resolve(command.path) ?: return

                engineScope.launch {
                    bridge.saveGameState(saveFile)
                }
            }

            is EngineCommand.LoadSave -> {
                val saveFile = vfsProvider.resolve(command.path) ?: return

                engineScope.launch {
                    bridge.loadGameState(saveFile)
                    bridge.executeCounter()
                }
            }

            else -> Unit
        }
    }

    override fun onStateRefreshed(
        mainDesc: String,
        varsDesc: String,
        uiConfig: LibUIConfig,
        actions: List<RawItem>,
        objects: List<RawItem>
    ) {
        _state.update { current ->
            val currentRunning = current as? GameState.Running

            GameState.Running(
                gameId = currentRunning?.gameId ?: GameId(1),
                mainDescription = mainDesc,
                varsDescription = varsDesc,
                uiConfig = uiConfig,
                actions = actions.map { InteractableItem(it.text, resolveVfs(it.imagePath)) },
                objects = objects.map { InteractableItem(it.text, resolveVfs(it.imagePath)) },
                activeDialog = currentRunning?.activeDialog,
                activeWindows =
                    currentRunning?.activeWindows ?: setOf(
                        WindowType.MAIN, WindowType.INPUT, WindowType.ACTS
                    )
            )
        }
    }

    private fun updateDialogState(dialog: EngineDialog?) {
        _state.update { current ->
            when (current) {
                is GameState.Idle -> current.copy(activeDialog = dialog)
                is GameState.Loading -> current.copy(activeDialog = dialog)
                is GameState.Running -> current.copy(activeDialog = dialog)
            }
        }
    }

    private fun showBlockingDialog(dialog: EngineDialog) =
        runBlocking {
            isUiBlocked.value = true
            val deferred = CompletableDeferred<Unit>()
            pendingMessageDeferred = deferred

            updateDialogState(dialog)

            deferred.await()

            isUiBlocked.value = false
        }

    override fun onError(details: String) = showBlockingDialog(EngineDialog.Error(details))

    override fun onShowMessage(text: String) = showBlockingDialog(EngineDialog.Message(text))

    override fun onShowImage(path: String): Unit = showBlockingDialog(
        EngineDialog.Picture(resolveVfs(path) ?: return)
    )

    override fun onInputBox(prompt: String): String = runBlocking {
        isUiBlocked.value = true
        val deferred = CompletableDeferred<String>()
        pendingInputDeferred = deferred

        updateDialogState(EngineDialog.Input(prompt))

        val result = deferred.await()
        isUiBlocked.value = false
        result
    }

    override fun onShowMenu(items: List<RawItem>): Int = runBlocking {
        isUiBlocked.value = true
        val deferred = CompletableDeferred<Int>()
        pendingMenuDeferred = deferred

        updateDialogState(
            EngineDialog.Menu(
                items.map {
                    InteractableItem(it.text, resolveVfs(it.imagePath))
                }
            )
        )

        val result = deferred.await()
        isUiBlocked.value = false
        result
    }

    override fun onShowWindow(type: WindowType, isVisible: Boolean) {
        _state.update { current ->
            if (current !is GameState.Running) return@update current

            val newWindows = if (isVisible) {
                current.activeWindows + type
            } else {
                current.activeWindows - type
            }

            current.copy(activeWindows = newWindows)
        }
    }

    override fun onOpenGame(
        path: String,
        isGame: Boolean
    ) = runBlocking {
        val file = if (isGame) {
            vfsProvider.resolve(VfsPath(path))
        } else {
            vfsProvider.resolveRelative(path)
        }

        if (file == null) {
            onError("File not found: $path")
            return@runBlocking
        }

        if (!isGame) {
            val isLoaded = bridge.loadGame(file, isGame = false)
            if (!isLoaded) {
                onError("Failed to load extension: $path")
            }
        } else {
            _state.value = GameState.Loading()
            audioService.closeAllFiles()
            vfsProvider.setGameRoot(file.path)

            val isLoaded = bridge.loadGame(file, isGame = true)
            if (isLoaded) {
                bridge.restartGame()
            } else {
                _state.value = GameState.Idle()
                onError("Failed to load game: $path")
            }
        }
    }

    override fun onSaveGame(
        path: String
    ) = runBlocking {
        bridge.saveGameState(
            vfsProvider.resolveRelative(path) ?: return@runBlocking
        )
    }

    override fun onLoadSave(
        path: String
    ) = runBlocking {
        bridge.loadGameState(
            vfsProvider.resolveRelative(path) ?: return@runBlocking
        )
    }

    override fun onCloseAudio(path: String?) {
        if (path.isNullOrBlank()) {
            audioService.closeAllFiles()
        } else {
            resolveVfs(path)?.let { audioService.closeFile(it.value) }
        }
    }

    override fun onCheckAudioPlaying(path: String): Boolean {
        val vfsPath = resolveVfs(path) ?: return false
        return audioService.isPlayingFile(vfsPath.value)
    }

    override fun onPlayAudio(path: String, volume: Int) {
        resolveVfs(path)?.let { audioService.playFile(it.value, volume) }
    }

    private fun resolveVfs(rawPath: String): VfsPath? {
        if (rawPath.isBlank()) return null
        return vfsProvider.resolveRelative(rawPath)?.path
    }
}