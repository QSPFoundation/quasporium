package com.pixnpunk.game.data

import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.file.MimeType.BINARY_FILE
import com.anggrayudi.storage.file.makeFile
import com.pixnpunk.audio.AudioPlayerViewModel
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.game.data.impl.QSLibSDHImpl
import com.pixnpunk.game.data.impl.QSLibSNXImpl
import com.pixnpunk.game.data.impl.QSPLibImpl
import com.pixnpunk.game.domain.model.LibGameState
import com.pixnpunk.game.domain.model.LibReturnValue
import com.pixnpunk.game.domain.model.LibTypeDialog
import com.pixnpunk.game.domain.model.LibTypePopup
import com.pixnpunk.game.domain.model.LibTypeWindow
import com.pixnpunk.game.domain.model.LibUIConfig
import com.pixnpunk.game.domain.repository.GameInterface
import com.pixnpunk.settings.data.repository.SettingsRepository
import com.pixnpunk.utils.FileUtil.isWritableDir
import com.pixnpunk.utils.FileUtil.isWritableFile
import com.pixnpunk.utils.PathUtil.absToRelPath
import com.pixnpunk.utils.PathUtil.normalizeContentPath
import com.pixnpunk.utils.PathUtil.pathToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.concurrent.atomic.AtomicLong
import kotlin.contracts.ExperimentalContracts

class NativeViewModel(
    private val audioPlayer: AudioPlayerViewModel
) : ViewModel(), GameInterface, KoinComponent {

    private var counterNativeByteJob: Job? = null
    private var counterNativeSonnixJob: Job? = null
    private var counterNativeSeedhartaJob: Job? = null
    private var updateSettingsJob: Job? = null

    private var mLibAuthors: NativeLibAuthors = NativeLibAuthors.BYTE

    private val libNativeByte = QSPLibImpl(get(), this)
    private val libNativeSonnix = QSLibSNXImpl(get(), this)
    private val libNativeSeedharta = QSLibSDHImpl(get(), this)

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
    private val counterNativeByteTask: suspend CoroutineScope.() -> Unit = {
        while (isActive) {
            libNativeByte.executeCounter()
            delay(counterInterval.get())
        }
    }
    private val counterNativeSonnixTask: suspend CoroutineScope.() -> Unit = {
        while (isActive) {
            libNativeSonnix.executeCounter()
            delay(counterInterval.get())
        }
    }
    private val counterNativeSeedhartaTask: suspend CoroutineScope.() -> Unit = {
        while (isActive) {
            libNativeSeedharta.executeCounter()
            delay(counterInterval.get())
        }
    }

    override fun setGameState(gameState: LibGameState) {
        _gameStateFlow.value = gameState
    }

    override fun setUIConfig(uiConfig: LibUIConfig) {
        _gameUIConfFlow.value = uiConfig
    }

    fun putReturnValue(returnValue: LibReturnValue?) {
        if (returnValue == null) {
            when (mLibAuthors) {
                NativeLibAuthors.BYTE ->
                    libNativeByte.returnValueFuture.completeExceptionally(
                        GameInterface.UserCancelledException()
                    )

                NativeLibAuthors.SONNIX ->
                    libNativeSonnix.returnValueFuture.completeExceptionally(
                        GameInterface.UserCancelledException()
                    )

                NativeLibAuthors.SEEDHARTHA ->
                    libNativeSeedharta.returnValueFuture.completeExceptionally(
                        GameInterface.UserCancelledException()
                    )
            }
        } else {
            when (mLibAuthors) {
                NativeLibAuthors.BYTE ->
                    libNativeByte.returnValueFuture.complete(returnValue)

                NativeLibAuthors.SONNIX ->
                    libNativeSonnix.returnValueFuture.complete(returnValue)

                NativeLibAuthors.SEEDHARTHA ->
                    libNativeSeedharta.returnValueFuture.complete(returnValue)
            }
        }
    }

    fun startService(
        settingsRepository: SettingsRepository,
        gameId: Long,
        gameTitle: String,
        gameDirUriBox: String,
        gameFileUriBox: String
    ) {
        this.gameDirUri = gameDirUriBox.toUri()
        this.mLibAuthors = settingsRepository.currentSettings.nativeLibFrom

        when (mLibAuthors) {
            NativeLibAuthors.BYTE -> {
                libNativeByte.startLibThread()
                libNativeByte.runGame(
                    gameId,
                    gameTitle,
                    gameDirUriBox,
                    gameFileUriBox
                )
            }

            NativeLibAuthors.SONNIX -> {
                libNativeSonnix.startLibThread()
                libNativeSonnix.runGame(
                    gameId,
                    gameTitle,
                    gameDirUriBox,
                    gameFileUriBox
                )
            }

            NativeLibAuthors.SEEDHARTHA -> {
                libNativeSeedharta.startLibThread()
                libNativeSeedharta.runGame(
                    gameId,
                    gameTitle,
                    gameDirUriBox,
                    gameFileUriBox
                )
            }
        }

        updateSettingsJob = viewModelScope.launch {
            settingsRepository.settingsFlow.combine(gameUIConfFlow) { appSet, libSet ->
                appSet.copy(
                    isUseHtml = libSet.useHtml,
                    textColor = if (!appSet.isTextColorOverride && libSet.fontColor != 0) {
                        libSet.fontColor
                    } else {
                        appSet.textColor
                    },
                    backColor = if (!appSet.isBackColorOverride && libSet.backColor != 0) {
                        libSet.backColor
                    } else {
                        appSet.backColor
                    },
                    linkColor = if (!appSet.isLinkColorOverride && libSet.linkColor != 0) {
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
            }.collect { settingsRepository.emitValue(it) }
        }
    }

    override fun onCleared() {
        super.onCleared()

        listOf(
            libNativeByte,
            libNativeSonnix,
            libNativeSeedharta
        ).forEach { it.stopLibThread() }

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
    override fun requestReceiveFile(filePath: String) {
        val gameDir = gameDirUri.toDocumentFile(get())
        if (!gameDir.isWritableDir()) {
            putReturnValue(LibReturnValue())
            return
        }

        val docFile = filePath.pathToFile(get(), gameDir)
        if (!docFile.isWritableFile()) {
            putReturnValue(LibReturnValue())
        } else {
            putReturnValue(LibReturnValue(filePath = docFile.uri.toString()))
        }
    }

    @OptIn(ExperimentalContracts::class)
    override fun requestReceiveFile() {
        val gameDir = gameDirUri.toDocumentFile(get())
        if (!gameDir.isWritableDir()) {
            putReturnValue(LibReturnValue())
            return
        }

        showLibPopup(LibTypePopup.POPUP_LOAD)
    }

    @OptIn(ExperimentalContracts::class)
    @WorkerThread
    override fun requestCreateFile(path: String) {
        val gameDir = gameDirUri.toDocumentFile(get())
        if (!gameDir.isWritableDir()) {
            putReturnValue(LibReturnValue())
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val file = gameDir.makeFile(
                get(),
                path.absToRelPath(get(), gameDir),
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
        val gameDir = gameDirUri.toDocumentFile(get())
        if (!gameDir.isWritableDir()) {
            putReturnValue(LibReturnValue())
            return
        }

        showLibPopup(LibTypePopup.POPUP_SAVE)
    }

    @OptIn(ExperimentalContracts::class)
    override fun isPlayingFile(filePath: String): Boolean {
        val gameDir = gameDirUri.toDocumentFile(get())
        if (gameDir.isWritableDir()) {
            val docFile = filePath
                .normalizeContentPath()
                .pathToFile(get(), gameDir)
            if (docFile.isWritableFile()) {
                return audioPlayer.isPlayingFile(docFile.uri)
            }
        }
        return false
    }

    override fun closeAllFiles() = runCatching {
        audioPlayer.closeAllFiles()
    }

    @OptIn(ExperimentalContracts::class)
    override fun closeFile(filePath: String) {
        val gameDir = gameDirUri.toDocumentFile(get())
        if (gameDir.isWritableDir()) {
            val docFile = filePath
                .normalizeContentPath()
                .pathToFile(get(), gameDir)
            if (docFile.isWritableFile()) {
                audioPlayer.closeFile(docFile.uri)
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    override fun playFile(path: String, volume: Int) {
        val gameDir = gameDirUri.toDocumentFile(get())
        if (gameDir.isWritableDir()) {
            val docFile = path
                .normalizeContentPath()
                .pathToFile(get(), gameDir)
            if (docFile.isWritableFile()) {
                audioPlayer.playFile(get(), docFile.uri, volume)
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    override fun changeGameDir(filePath: String) {
        // do nothing
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

    private fun showLibPopup(popupType: LibTypePopup) {
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
        counterInterval.set(delayMillis)
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