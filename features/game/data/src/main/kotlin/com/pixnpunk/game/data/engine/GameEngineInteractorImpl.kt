package com.pixnpunk.game.data.engine

import com.pixnpunk.game.data.engine.jni.NativeEngineBridge
import com.pixnpunk.game.data.engine.jni.NativeEngineListener
import com.pixnpunk.game.data.model.RawItem
import com.pixnpunk.game.data.vfs.FileSystemProvider
import com.pixnpunk.game.data.vfs.VirtualFile
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
import kotlin.collections.map
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
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

    private val _state = MutableStateFlow<GameState>(GameState.Idle)
    override val state = _state.asStateFlow()

    private val isUiBlocked = atomic(false)
    private val counterIntervalMs = atomic(500L)

    private var pendingInputDeferred: CompletableDeferred<String>? = null
    private var pendingMenuDeferred: CompletableDeferred<Int>? = null

    init {
        bridge.setListener(this)
        bridge.initializeEngine()

        startActorLoop()
        startGameLoopTimer()
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

    override fun onError(details: String) {
        _state.update { current ->
            if (current is GameState.Running) {
                current.copy(activeDialog = EngineDialog.Error(details))
            } else current
        }
    }

    override fun submitCommand(command: EngineCommand) {
        when (command) {
            is EngineCommand.SubmitInput -> {
                pendingInputDeferred?.complete(command.text)
                pendingInputDeferred = null
                dismissDialog()
            }

            is EngineCommand.SelectMenuItem -> {
                pendingMenuDeferred?.complete(command.index)
                pendingMenuDeferred = null
                dismissDialog()
            }

            is EngineCommand.DismissDialog -> dismissDialog()
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
                _state.value = GameState.Loading
                audioService.closeAllFiles()

                engineScope.launch { bridge.restartGame() }
            }

            is EngineCommand.LoadGame -> {
                val file = vfsProvider.resolve(command.path) ?: return

                _state.value = GameState.Loading
                audioService.closeAllFiles()
                vfsProvider.setGameRoot(file.path)

                engineScope.launch {
                    val isLoaded = bridge.loadGame(file)
                    if (isLoaded) {
                        bridge.restartGame()
                    } else {
                        _state.value = GameState.Idle
                    }
                }
            }

            is EngineCommand.LoadGameFile -> {
                val file = vfsProvider.resolve(command.path) ?: return

                _state.value = GameState.Loading

                engineScope.launch {
                    bridge.loadGame(
                        file = file,
                        isNewGame = command.isNewGame
                    )
                }
            }

            is EngineCommand.SaveGame -> {
                engineScope.launch {
                    val saveFile = vfsProvider.resolve(command.path) ?: return@launch
                    bridge.saveGameState(saveFile)
                }
            }

            is EngineCommand.LoadSave -> {
                engineScope.launch {
                    val saveFile = vfsProvider.resolve(command.path) ?: return@launch
                    bridge.loadGameState(saveFile)
                    bridge.executeCounter()
                }
            }

            else -> Unit
        }
    }

    private fun dismissDialog() {
        _state.update { current ->
            if (current is GameState.Running) current.copy(activeDialog = null) else current
        }
    }

    override fun onStateRefreshed(
        mainDesc: String,
        varsDesc: String,
        uiConfig: LibUIConfig,
        actions: List<RawItem>,
        objects: List<RawItem>
    ) {
        _state.update {
            GameState.Running(
                gameId = GameId(1),
                mainDescription = mainDesc,
                varsDescription = varsDesc,
                uiConfig = uiConfig,
                actions = actions.map { InteractableItem(it.text, resolveVfs(it.imagePath)) },
                objects = objects.map { InteractableItem(it.text, resolveVfs(it.imagePath)) }
            )
        }
    }

    override fun onShowMessage(text: String) {
        _state.update { current ->
            if (current is GameState.Running) {
                current.copy(activeDialog = EngineDialog.Message(text))
            } else current
        }
    }

    override fun onShowImage(path: String) {
        val vfsPath = resolveVfs(path) ?: return
        _state.update { current ->
            if (current is GameState.Running) current.copy(
                activeDialog = EngineDialog.Picture(
                    vfsPath
                )
            )
            else current
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    override fun onInputBox(prompt: String): String = runBlocking {
        isUiBlocked.value = true
        val deferred = CompletableDeferred<String>()
        pendingInputDeferred = deferred

        _state.update { current ->
            if (current is GameState.Running) current.copy(activeDialog = EngineDialog.Input(prompt))
            else current
        }

        val result = deferred.await()
        isUiBlocked.value = false
        result
    }

    @OptIn(ExperimentalAtomicApi::class)
    override fun onShowMenu(items: List<RawItem>): Int = runBlocking {
        isUiBlocked.value = true
        val deferred = CompletableDeferred<Int>()
        pendingMenuDeferred = deferred

        val menuItems = items.map { InteractableItem(it.text, resolveVfs(it.imagePath)) }
        _state.update { current ->
            if (current is GameState.Running) current.copy(
                activeDialog = EngineDialog.Menu(
                    menuItems
                )
            )
            else current
        }

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

    override fun onOpenGame(path: String, isNewGame: Boolean) {
        processCommand(EngineCommand.LoadGameFile(VfsPath(path), isNewGame))
    }

    override fun onSaveGame(path: String) {
        processCommand(EngineCommand.SaveGame(VfsPath(path)))
    }

    override fun onLoadSave(path: String) {
        processCommand(EngineCommand.LoadSave(VfsPath(path)))
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

    override fun shutdown() {
        engineScope.cancel()
        bridge.destroyEngine()
    }

    private fun resolveVfs(rawPath: String): VfsPath? {
        if (rawPath.isBlank()) return null
        return vfsProvider.resolveRelative(rawPath)?.path
    }
}