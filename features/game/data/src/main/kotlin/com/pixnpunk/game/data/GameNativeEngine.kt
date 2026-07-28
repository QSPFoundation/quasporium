package com.pixnpunk.game.data

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.file.MimeType.BINARY_FILE
import com.anggrayudi.storage.file.makeFile
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.game.domain.model.LibGameState
import com.pixnpunk.game.domain.model.LibReturnValue
import com.pixnpunk.game.domain.model.LibTypeDialog
import com.pixnpunk.game.domain.model.LibTypePopup
import com.pixnpunk.game.domain.model.LibTypeWindow
import com.pixnpunk.game.domain.model.LibUIConfig
import com.pixnpunk.game.domain.repository.GameInterface
import com.pixnpunk.game.domain.repository.LibIProxy
import com.pixnpunk.settings.dto.repository.SettingsRepository
import com.pixnpunk.utils.FileUtil.isWritableDir
import com.pixnpunk.utils.FileUtil.isWritableFile
import com.pixnpunk.utils.PathUtil.absToRelPath
import com.pixnpunk.utils.PathUtil.normalizeContentPath
import com.pixnpunk.utils.PathUtil.pathToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import kotlin.contracts.ExperimentalContracts

class GameNativeEngine(
    private val context: Context,
    private val audioPlayer: GameAudioComponent,
    private val libNewFactory: (GameInterface) -> LibIProxy,
    private val libOldFactory: (GameInterface) -> LibIProxy,
    private val settingsRepository: SettingsRepository
) : GameInterface, InstanceKeeper.Instance {

    private val engineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var counterJob: Job? = null

    private var mLibAuthors: NativeLibAuthors = NativeLibAuthors.BYTE

    private val libNativeNew by lazy { libNewFactory(this) }
    private val libNativeOld by lazy { libOldFactory(this) }

    private val activeLib: LibIProxy
        get() = when (mLibAuthors) {
            NativeLibAuthors.BYTE -> libNativeNew
            NativeLibAuthors.SEEDHARTHA -> libNativeOld
        }

    private var gameDirUri: Uri = Uri.EMPTY

    private val _gameStateFlow: MutableStateFlow<LibGameState> = MutableStateFlow(LibGameState())
    val gameStateFlow = _gameStateFlow.asStateFlow()
    private val _gameUIConfFlow: MutableStateFlow<LibUIConfig> = MutableStateFlow(LibUIConfig())
    val gameUIConfFlow = _gameUIConfFlow.asStateFlow()

    private val _gameDialogFlow: MutableSharedFlow<Triple<LibTypeDialog, String, List<LibGenItem>>> = MutableSharedFlow()
    val gameDialogFlow = _gameDialogFlow.asSharedFlow()
    private val _gamePopupFlow: MutableSharedFlow<LibTypePopup> = MutableSharedFlow()
    val gamePopupFlow = _gamePopupFlow.asSharedFlow()
    private val _gameElementVis: MutableSharedFlow<Pair<LibTypeWindow, Boolean>> = MutableSharedFlow()
    val gameElementVis = _gameElementVis.asSharedFlow()

    private val counterInterval = AtomicLong(500L)

    override fun setGameState(gameState: LibGameState) {
        _gameStateFlow.value = gameState
    }

    override fun setUIConfig(uiConfig: LibUIConfig) {
        _gameUIConfFlow.value = uiConfig
    }

    fun putReturnValue(returnValue: LibReturnValue?) {
        if (returnValue != null) {
            activeLib.returnValueDeferred.complete(returnValue)
        } else {
            activeLib.returnValueDeferred.completeExceptionally(
                GameInterface.UserCancelledException()
            )
        }
    }

    fun startService(
        gameId: Long,
        gameTitle: String,
        gameDirUriBox: String,
        gameFileUriBox: String
    ) {
        this.gameDirUri = gameDirUriBox.toUri()
        this.mLibAuthors = settingsRepository.settingsFlow.value.nativeLibFrom

        activeLib.startLibThread()
        activeLib.runGame(
            gameId,
            gameTitle,
            gameDirUriBox,
            gameFileUriBox
        )
    }

    override fun onDestroy() {
        runCatching { libNativeNew.stopLibThread() }
        runCatching { libNativeOld.stopLibThread() }
        engineScope.cancel()
    }

    fun onUseInputArea(inputString: String) = activeLib.onInputAreaClicked(inputString)
    fun onSaveFile(fileUri: Uri) = activeLib.saveGameState(fileUri.toString())
    fun onCodeExec(execCode: String) = activeLib.execute(execCode)
    fun onActionClicked(index: Int) = activeLib.onActionClicked(index)
    fun onObjectSelected(index: Int) = activeLib.onObjectSelected(index)

    fun onLoadFile(fileUri: Uri) {
        doWithCounterDisabled {
            when (mLibAuthors) {
                NativeLibAuthors.BYTE -> {
                    libNativeNew.loadGameState(fileUri.toString())
                }

                NativeLibAuthors.SEEDHARTHA -> {
                    libNativeOld.loadGameState(fileUri.toString())
                }
            }
        }
    }

    fun onRestartGame() {
        closeAllFiles()
        activeLib.restartGame()
    }

    @OptIn(ExperimentalContracts::class)
    override fun requestReceiveFile(filePath: String) {
        val gameDir = gameDirUri.toDocumentFile(context)
        if (!gameDir.isWritableDir()) {
            putReturnValue(LibReturnValue())
            return
        }

        val docFile = filePath.pathToFile(context, gameDir)
        if (!docFile.isWritableFile()) {
            putReturnValue(LibReturnValue())
        } else {
            putReturnValue(LibReturnValue(filePath = docFile.uri.toString()))
        }
    }

    @OptIn(ExperimentalContracts::class)
    override fun requestReceiveFile() {
        val gameDir = gameDirUri.toDocumentFile(context)
        if (!gameDir.isWritableDir()) {
            putReturnValue(LibReturnValue())
            return
        }

        showLibPopup(LibTypePopup.POPUP_LOAD)
    }

    @OptIn(ExperimentalContracts::class)
    @WorkerThread
    override fun requestCreateFile(path: String) {
        val gameDir = gameDirUri.toDocumentFile(context)
        if (!gameDir.isWritableDir()) {
            putReturnValue(LibReturnValue())
            return
        }

        engineScope.launch(Dispatchers.IO) {
            val file = gameDir.makeFile(
                context,
                path.absToRelPath(context, gameDir),
                BINARY_FILE,
                CreateMode.REUSE
            )
            if (file.isWritableFile()) {
                putReturnValue(LibReturnValue(filePath = file.uri.toString()))
            } else {
                putReturnValue(LibReturnValue(filePath = Uri.EMPTY.toString()))
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    override fun requestCreateFile() {
        val gameDir = gameDirUri.toDocumentFile(context)
        if (!gameDir.isWritableDir()) {
            putReturnValue(LibReturnValue())
            return
        }

        showLibPopup(LibTypePopup.POPUP_SAVE)
    }

    @OptIn(ExperimentalContracts::class)
    override fun isPlayingFile(filePath: String): Boolean {
        val gameDir = gameDirUri.toDocumentFile(context)
        if (gameDir.isWritableDir()) {
            val docFile = filePath
                .normalizeContentPath()
                .pathToFile(context, gameDir)
            if (docFile.isWritableFile()) {
                return audioPlayer.isPlayingFile(docFile.uri.toString())
            }
        }
        return false
    }

    override fun closeAllFiles() = runCatching {
        audioPlayer.closeAllFiles()
    }

    @OptIn(ExperimentalContracts::class)
    override fun closeFile(filePath: String) {
        val gameDir = gameDirUri.toDocumentFile(context)
        if (gameDir.isWritableDir()) {
            val docFile = filePath
                .normalizeContentPath()
                .pathToFile(context, gameDir)
            if (docFile.isWritableFile()) {
                audioPlayer.closeFile(
                    docFile.uri.toString()
                )
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    override fun playFile(path: String, volume: Int) {
        val gameDir = gameDirUri.toDocumentFile(context)
        if (gameDir.isWritableDir()) {
            val docFile = path
                .normalizeContentPath()
                .pathToFile(context, gameDir)
            if (docFile.isWritableFile()) {
                audioPlayer.playFile(
                    docFile.uri.toString(),
                    volume
                )
            }
        }
    }

    override fun showLibDialog(
        dialogType: LibTypeDialog,
        inputString: String,
        menuItems: List<LibGenItem>
    ) {
        engineScope.launch { _gameDialogFlow.emit(Triple(dialogType, inputString, menuItems)) }
    }

    private fun showLibPopup(popupType: LibTypePopup) {
        engineScope.launch { _gamePopupFlow.emit(popupType) }
    }

    override fun changeVisWindow(
        type: LibTypeWindow,
        show: Boolean
    ) {
        engineScope.launch { _gameElementVis.emit(type to show) }
    }

    override fun setCountInter(delayMillis: Long) {
        counterInterval.set(delayMillis)
    }

    override fun doWithCounterDisabled(runnable: Runnable) {
        counterJob?.cancel()
        runnable.run()
        counterJob = engineScope.launch {
            while (isActive) {
                activeLib.executeCounter()
                delay(counterInterval.get())
            }
        }
    }
}
