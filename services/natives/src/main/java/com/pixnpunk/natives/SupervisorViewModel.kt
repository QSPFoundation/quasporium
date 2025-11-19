package com.pixnpunk.natives

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.file.DocumentFileCompat.doesExist
import com.anggrayudi.storage.file.makeFile
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.pixnpunk.audio.AudioPlayerViewModel
import com.pixnpunk.dto.GameInterface
import com.pixnpunk.dto.LibGameState
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.LibReturnValue
import com.pixnpunk.dto.LibTypeDialog
import com.pixnpunk.dto.LibTypePopup
import com.pixnpunk.dto.LibTypeWindow
import com.pixnpunk.dto.LibUIConfig
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.natives.impl.QSLibSDHImpl
import com.pixnpunk.natives.impl.QSLibSNXImpl
import com.pixnpunk.natives.impl.QSPLibImpl
import com.pixnpunk.settings.SettingsRepository
import com.pixnpunk.utils.FileUtil.isWritableDir
import com.pixnpunk.utils.FileUtil.isWritableFile
import com.pixnpunk.utils.PathUtil.absToRelPath
import com.pixnpunk.utils.PathUtil.normalizeContentPath
import com.pixnpunk.utils.PathUtil.pathToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.CompletableFuture
import kotlin.contracts.ExperimentalContracts

class SupervisorViewModel(
    private val audioPlayer: AudioPlayerViewModel,
    private val settingsRepo: SettingsRepository
) : ViewModel(), GameInterface, KoinComponent {

    private val context: Context by inject()

    private var counterNativeByteJob: Job? = null
    private var counterNativeSonnixJob: Job? = null
    private var counterNativeSeedhartaJob: Job? = null
    private var updateSettingsJob: Job? = null

    private val mLibAuthors: NativeLibAuthors
        get() = settingsRepo.settingsFlow.value.nativeLibFrom
    private val libNativeByte = QSPLibImpl(context, this)
    private val libNativeSonnix = QSLibSNXImpl(context, this)
    private val libNativeSeedharta = QSLibSDHImpl(context, this)

    private var gameDirUri: Uri = Uri.EMPTY

    private val _gameStateFlow: MutableStateFlow<LibGameState> = MutableStateFlow(LibGameState())
    val gameStateFlow = _gameStateFlow.asStateFlow()
    private val _gameUIConfFlow: MutableStateFlow<LibUIConfig> = MutableStateFlow(LibUIConfig())
    val gameUIConfFlow = _gameUIConfFlow.asStateFlow()

    private val _gameDialogFlow: MutableSharedFlow<Triple<LibTypeDialog, String, List<LibGenItem>>> =
        MutableSharedFlow()
    private val _gamePopupFlow: MutableSharedFlow<LibTypePopup> = MutableSharedFlow()
    private val _gameElementVis: MutableSharedFlow<Pair<LibTypeWindow, Boolean>> =
        MutableSharedFlow()
    val gameDialogFlow: SharedFlow<Triple<LibTypeDialog, String, List<LibGenItem>>> =
        _gameDialogFlow
    val gamePopupFlow: SharedFlow<LibTypePopup> = _gamePopupFlow
    val gameElementVis: SharedFlow<Pair<LibTypeWindow, Boolean>> = _gameElementVis

    companion object {
        const val RECEIVE_FLOW_TIMEOUT: Long = 10000
    }

    @Volatile
    private var counterInterval = 500L
    private val counterNativeByteTask: suspend CoroutineScope.() -> Unit = {
        while (isActive) {
            libNativeByte.executeCounter()
            delay(counterInterval)
        }
    }
    private val counterNativeSonnixTask: suspend CoroutineScope.() -> Unit = {
        while (isActive) {
            libNativeSonnix.executeCounter()
            delay(counterInterval)
        }
    }
    private val counterNativeSeedhartaTask: suspend CoroutineScope.() -> Unit = {
        while (isActive) {
            libNativeSeedharta.executeCounter()
            delay(counterInterval)
        }
    }

    override fun setGameState(gameState: LibGameState) {
        _gameStateFlow.value = gameState
    }

    override fun setUIConfig(uiConfig: LibUIConfig) {
        _gameUIConfFlow.value = uiConfig
    }

    fun putReturnValue(returnValue: LibReturnValue) {
        when (mLibAuthors) {
            NativeLibAuthors.BYTE -> libNativeByte.returnValueFuture.complete(returnValue)

            NativeLibAuthors.SONNIX -> libNativeSonnix.returnValueFuture.complete(returnValue)

            NativeLibAuthors.SEEDHARTHA -> libNativeSeedharta.returnValueFuture.complete(returnValue)
        }
    }

    fun startService(
        gameId: Long,
        gameTitle: String,
        gameDirUri: Uri,
        gameFileUri: Uri
    ) {
        this.gameDirUri = gameDirUri

        when (mLibAuthors) {
            NativeLibAuthors.BYTE -> {
                libNativeByte.startLibThread()
                libNativeByte.runGame(gameId, gameTitle, gameDirUri.toString(), gameFileUri.toString())
            }

            NativeLibAuthors.SONNIX -> {
                libNativeSonnix.startLibThread()
                libNativeSonnix.runGame(gameId, gameTitle, gameDirUri.toString(), gameFileUri.toString())
            }

            NativeLibAuthors.SEEDHARTHA -> {
                libNativeSeedharta.startLibThread()
                libNativeSeedharta.runGame(gameId, gameTitle, gameDirUri.toString(), gameFileUri.toString())
            }
        }

        updateSettingsJob = viewModelScope.launch {
            settingsRepo.settingsFlow.combine(gameUIConfFlow) { appSet, libSet ->
                appSet.copy(
                    isUseHtml = libSet.useHtml,
                    textColor = if (appSet.isUseGameTextColor && libSet.fontColor != 0) {
                        libSet.fontColor
                    } else {
                        appSet.textColor
                    },
                    backColor = if (appSet.isUseGameBackgroundColor && libSet.backColor != 0) {
                        libSet.backColor
                    } else {
                        appSet.backColor
                    },
                    linkColor = if (appSet.isUseGameLinkColor && libSet.linkColor != 0) {
                        libSet.linkColor
                    } else {
                        appSet.linkColor
                    },
                    fontSize = if (appSet.isUseGameFont && libSet.fontSize != 0) {
                        libSet.fontSize
                    } else {
                        appSet.fontSize
                    }
                )
            }.collect { settingsRepo.emitValue(it) }
        }
    }

    override fun onCleared() {
        libNativeByte.stopLibThread()
        libNativeSonnix.stopLibThread()
        libNativeSeedharta.stopLibThread()
        updateSettingsJob?.cancel()
        counterNativeByteJob?.cancel()
        counterNativeSonnixJob?.cancel()
        counterNativeSeedhartaJob?.cancel()
    }

    fun onUseInputArea(inputString: String) {
        when (mLibAuthors) {
            NativeLibAuthors.BYTE -> {
                libNativeByte.onInputAreaClicked(inputString)
            }

            NativeLibAuthors.SONNIX -> {
                libNativeSonnix.onInputAreaClicked(inputString)
            }

            NativeLibAuthors.SEEDHARTHA -> {
                libNativeSeedharta.onInputAreaClicked(inputString)
            }
        }
    }

    fun onSaveFile(fileUri: Uri) {
        when (mLibAuthors) {
            NativeLibAuthors.BYTE -> {
                libNativeByte.saveGameState(fileUri.toString())
            }

            NativeLibAuthors.SONNIX -> {
                libNativeSonnix.saveGameState(fileUri.toString())
            }

            NativeLibAuthors.SEEDHARTHA -> {
                libNativeSeedharta.saveGameState(fileUri.toString())
            }
        }
    }

    fun onLoadFile(fileUri: Uri) {
        doWithCounterDisabled {
            when (mLibAuthors) {
                NativeLibAuthors.BYTE -> {
                    libNativeByte.loadGameState(fileUri.toString())
                }

                NativeLibAuthors.SONNIX -> {
                    libNativeSonnix.loadGameState(fileUri.toString())
                }

                NativeLibAuthors.SEEDHARTHA -> {
                    libNativeSeedharta.loadGameState(fileUri.toString())
                }
            }
        }
    }

    fun onCodeExec(execCode: String) {
        when (mLibAuthors) {
            NativeLibAuthors.BYTE -> {
                libNativeByte.execute(execCode)
            }

            NativeLibAuthors.SONNIX -> {
                libNativeSonnix.execute(execCode)
            }

            NativeLibAuthors.SEEDHARTHA -> {
                libNativeSeedharta.execute(execCode)
            }
        }
    }

    fun onRestartGame() {
        when (mLibAuthors) {
            NativeLibAuthors.BYTE -> {
                libNativeByte.restartGame()
            }

            NativeLibAuthors.SONNIX -> {
                libNativeSonnix.restartGame()

            }

            NativeLibAuthors.SEEDHARTHA -> {
                libNativeSeedharta.restartGame()
            }
        }
    }

    fun onActionClicked(index: Int) {
        when (mLibAuthors) {
            NativeLibAuthors.BYTE -> {
                libNativeByte.onActionClicked(index)
            }

            NativeLibAuthors.SONNIX -> {
                libNativeSonnix.onActionClicked(index)
            }

            NativeLibAuthors.SEEDHARTHA -> {
                libNativeSeedharta.onActionClicked(index)
            }
        }
    }

    fun onObjectSelected(index: Int) {
        when (mLibAuthors) {
            NativeLibAuthors.BYTE -> {
                libNativeByte.onObjectSelected(index)
            }

            NativeLibAuthors.SONNIX -> {
                libNativeSonnix.onObjectSelected(index)
            }

            NativeLibAuthors.SEEDHARTHA -> {
                libNativeSeedharta.onObjectSelected(index)
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    override fun requestReceiveFile(filePath: String): String {
        val gameDir = gameDirUri.toDocumentFile(context)
        if (gameDir.isWritableDir()) {
            val docFile = filePath.pathToFile(context, gameDir)
            if (docFile.isWritableFile()) {
                return docFile.uri.toString()
            } else {
                Firebase.crashlytics.log("requestReceiveFile: ERROR!")
            }
        }
        return Uri.EMPTY.toString()
    }

    @OptIn(ExperimentalContracts::class)
    @WorkerThread
    override fun requestCreateFile(path: String, mimeType: String): String {
        val gameDir = gameDirUri.toDocumentFile(context)
        if (!gameDir.isWritableDir()) return Uri.EMPTY.toString()

        return CompletableFuture
            .supplyAsync {
                val relPath = path.absToRelPath(context, gameDir)
                gameDir.makeFile(context, relPath, mimeType, CreateMode.REUSE)
            }
            .thenApply {
                if (it.isWritableFile()) {
                    return@thenApply it.uri.toString()
                } else {
                    Firebase.crashlytics.log("requestCreateFile: ERROR!")
                    return@thenApply Uri.EMPTY.toString()
                }
            }
            .get()
    }

    @OptIn(ExperimentalContracts::class)
    override fun isPlayingFile(filePath: String): Boolean {
        val gameDir = gameDirUri.toDocumentFile(context)
        if (gameDir.isWritableDir()) {
            val docFile = filePath
                .normalizeContentPath()
                .pathToFile(context, gameDir)
            if (docFile.isWritableFile()) {
                return audioPlayer.isPlayingFile(docFile.uri)
            } else {
                val errorMsg = RuntimeException("Sound file by path: $filePath not writable")
                Firebase.crashlytics.log("isPlayingFile: ERROR! | Cause: $errorMsg")
            }
        }
        return false
    }

    override fun closeAllFiles() {
        try {
            audioPlayer.closeAllFiles()
        } catch (e: Exception) {
            Firebase.crashlytics.log("closeAllFiles: ERROR! | Cause: $e")
        }
    }

    @OptIn(ExperimentalContracts::class)
    override fun closeFile(filePath: String) {
        val gameDir = gameDirUri.toDocumentFile(context)
        if (gameDir.isWritableDir()) {
            val docFile = filePath
                .normalizeContentPath()
                .pathToFile(context, gameDir)
            if (docFile.isWritableFile()) {
                audioPlayer.closeFile(docFile.uri)
            } else {
                val errorMsg = RuntimeException("Sound file by path: $filePath not writable")
                Firebase.crashlytics.log("closeFile: ERROR! | Cause: $errorMsg")
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
                audioPlayer.playFile(context, docFile.uri, volume)
            } else {
                val errorMsg = RuntimeException("Sound file by path: $path not writable")
                Firebase.crashlytics.log("playFile: ERROR! | Cause: $errorMsg")
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    override fun changeGameDir(filePath: String) {
        val gameDir = gameDirUri.toDocumentFile(context)
        if (!gameDir.isWritableDir()) return

        CompletableFuture
            .supplyAsync { doesExist(context, filePath) }
            .thenAcceptAsync {
                if (it) {
//                    val oldDirPath = gameDir.getAbsolutePath(this)
//                    if (oldDirPath != filePath) {
//                        val newGameDir = fromFullPath(this, filePath, requiresWriteAccess = true)
//                        if (isWritableDir(this, newGameDir)) {
//                            gameDirUri = newGameDir.uri
//                        }
//                    }
                }
            }
    }

    override fun showLibDialog(
        dialogType: LibTypeDialog,
        inputString: String,
        menuItems: List<LibGenItem>
    ) {
        viewModelScope.launch {
            _gameDialogFlow.emit(Triple(dialogType, inputString, menuItems))
        }
    }

    override fun showLibPopup(popupType: LibTypePopup) {
        viewModelScope.launch {
            _gamePopupFlow.emit(popupType)
        }
    }

    override fun changeVisWindow(
        type: LibTypeWindow,
        show: Boolean
    ) {
        viewModelScope.launch {
            _gameElementVis.emit(type to show)
        }
    }

    override fun setCountInter(delayMillis: Long) {
        counterInterval = delayMillis
    }

    override fun doWithCounterDisabled(runnable: Runnable) {
        when (mLibAuthors) {
            NativeLibAuthors.BYTE -> {
                counterNativeByteJob?.cancel()
                runnable.run()
                counterNativeByteJob =
                    viewModelScope.launch(block = counterNativeByteTask)
            }

            NativeLibAuthors.SONNIX -> {
                counterNativeSonnixJob?.cancel()
                runnable.run()
                counterNativeSonnixJob =
                    viewModelScope.launch(block = counterNativeSonnixTask)
            }

            NativeLibAuthors.SEEDHARTHA -> {
                counterNativeSeedhartaJob?.cancel()
                runnable.run()
                counterNativeSeedhartaJob =
                    viewModelScope.launch(block = counterNativeSeedhartaTask)
            }
        }
    }
}