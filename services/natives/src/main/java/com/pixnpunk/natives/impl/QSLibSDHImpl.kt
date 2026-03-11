@file:OptIn(ExperimentalContracts::class)

package com.pixnpunk.natives.impl

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.SystemClock
import androidx.core.net.toUri
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.getAbsolutePath
import com.libsdhqs.jni.QSLibSDH
import com.pixnpunk.dto.GameInterface
import com.pixnpunk.dto.LibGameState
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.LibIProxy
import com.pixnpunk.dto.LibReturnValue
import com.pixnpunk.dto.LibTypeDialog
import com.pixnpunk.dto.LibUIConfig
import com.pixnpunk.natives.dto.toLibRawItem
import com.pixnpunk.utils.ThreadUtil.isSameThread
import java.util.concurrent.CompletableFuture
import kotlin.contracts.ExperimentalContracts

class QSLibSDHImpl(
    private val context: Context,
    override var gameInterface: GameInterface
) : QSLibSDH(), LibIProxy {

    private val delegate = LibDelegate(
        context = context,
        gameInterface = gameInterface,
        initLib = ::init,
        terminateLib = ::terminate
    )

    override var gameState: LibGameState
        get() = delegate.gameState
        set(value) { delegate.gameState = value }

    override var returnValueFuture: CompletableFuture<LibReturnValue>
        get() = delegate.returnValueFuture
        set(value) { delegate.returnValueFuture = value }

    private inline fun executeQspCommand(block: () -> Boolean): Boolean =
        if (block()) true else { showLastLibError(); false }

    private fun showLastLibError() {
        val errorData = getLastErrorData()
        gameInterface.showLibDialog(
            dialogType = LibTypeDialog.DIALOG_ERROR,
            inputString = """
                Location: ${errorData?.locName.orEmpty()}
                Action: ${errorData?.index}
                Line: ${errorData?.line}
                Error number: ${errorData?.errorNum}
                Description: ${errorData?.let { getErrorDesc(it.errorNum).orEmpty() }.orEmpty()}
            """.trimIndent()
        )
    }

    private fun loadGameWorld(): Boolean {
        val gameFileUri = delegate.gameState.gameFilePath.toUri()
        val gameFile = gameFileUri.toDocumentFile(context) ?: return false
        val gameFileFullPath = gameFile.getAbsolutePath(context)

        return try {
            context.contentResolver.openFileDescriptor(gameFileUri, "r")?.use { pfd ->
                executeQspCommand { loadGameWorldFromFD(pfd.fd, gameFileFullPath) }
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    private val actionsList: List<LibGenItem>
        get() = delegate.buildGameItemsList(
            getActions().orEmpty().filterNotNull().map { it.toLibRawItem() }
        )

    private val objectsList: List<LibGenItem>
        get() = delegate.buildGameItemsList(
            getObjects().orEmpty().filterNotNull().map { it.toLibRawItem() }
        )

    override fun startLibThread() = delegate.startLibThread("libNDKQSP")
    override fun stopLibThread() = delegate.stopLibThread()

    override fun runGame(
        gameId: Long,
        gameTitle: String,
        gameDirPath: String,
        gameFilePath: String
    ) {
        delegate.runOnLibThread {
            doRunGame(gameId, gameTitle, gameDirPath.toUri(), gameFilePath.toUri())
        }
    }

    override fun restartGame() {
        delegate.runOnLibThread {
            with(delegate.gameState) {
                doRunGame(gameId, gameTitle, gameDirPath.toUri(), gameFilePath.toUri())
            }
        }
    }

    private fun doRunGame(id: Long, title: String, dir: Uri, file: Uri) {
        gameInterface.doWithCounterDisabled {
            gameInterface.closeAllFiles()
            delegate.gameState = delegate.gameState.copy(
                gameRunning = true,
                gameId = id,
                gameTitle = title,
                gameDirPath = dir.toString(),
                gameFilePath = file.toString()
            )
            if (!loadGameWorld()) return@doWithCounterDisabled
            delegate.gameStartTime = SystemClock.elapsedRealtime()
            delegate.lastMsCountCallTime = 0
            executeQspCommand { restartGame(true) }
        }
    }

    override fun loadGameState(path: String) {
        if (!isSameThread(delegate.libHandler.looper.thread)) {
            delegate.runOnLibThread { loadGameState(path) }
            return
        }

        runCatching {
            context.contentResolver.openFileDescriptor(path.toUri(), "r")?.use {
                executeQspCommand { openSavedGameFromFD(it.fd, true) }
            }
        }.onFailure {  }
    }

    override fun saveGameState(path: String) {
        if (!isSameThread(delegate.libHandler.looper.thread)) {
            delegate.runOnLibThread { saveGameState(path) }
            return
        }

        runCatching {
            context.contentResolver.openFileDescriptor(path.toUri(), "w")?.use {
                executeQspCommand { saveGameByFD(it.fd, false) }
            }
        }.onFailure { }
    }

    override fun onActionClicked(index: Int) {
        delegate.runOnLibThread {
            executeQspCommand { setSelActionIndex(index, false) }
            executeQspCommand { executeSelActionCode(true) }
        }
    }

    override fun onObjectSelected(index: Int) {
        delegate.runOnLibThread { executeQspCommand { setSelObjectIndex(index, true) } }
    }

    override fun onInputAreaClicked(code: String) {
        delegate.runOnLibThread {
            setInputStrText(code)
            executeQspCommand { execUserInput(true) }
        }
    }

    override fun execute(code: String) {
        delegate.runOnLibThread { executeQspCommand { execString(code, true) } }
    }

    override fun executeCounter() {
        if (delegate.libLock.isLocked) return
        delegate.runOnLibThread { executeQspCommand { execCounter(true) } }
    }

    override fun onRefreshInt() {
        loadUIConfiguration()
        delegate.gameState = delegate.gameState.copy(
            mainDesc = getMainDesc().orEmpty(),
            varsDesc = getVarsDesc().orEmpty(),
            actionsList = actionsList,
            objectsList = objectsList
        )
        gameInterface.setGameState(delegate.gameState)
    }

    private fun loadUIConfiguration() {
        gameInterface.setUIConfig(
            LibUIConfig(
                useHtml = getVarValues("USEHTML", 0)?.intValue != 0,
                fontSize = getVarValues("FSIZE", 0)?.intValue ?: 0,
                backColor = getVarValues("BCOLOR", 0)?.intValue ?: 0,
                fontColor = getVarValues("FCOLOR", 0)?.intValue ?: 0,
                linkColor = getVarValues("LCOLOR", 0)?.intValue ?: 0
            )
        )
    }

    override fun onShowImage(path: String?) = delegate.handleShowImage(path)
    override fun onSetTimer(msecs: Int) = delegate.handleSetTimer(msecs)
    override fun onShowMessage(message: String?) = delegate.handleShowMessage(message)
    override fun onPlayFile(path: String?, volume: Int) = delegate.handlePlayFile(path, volume)
    override fun onIsPlayingFile(path: String?) = delegate.handleIsPlayingFile(path)
    override fun onCloseFile(path: String?) = delegate.handleCloseFile(path)
    override fun onSleep(msecs: Int) = delegate.handleSleep(msecs)
    override fun onGetMsCount() = delegate.handleGetMsCount()
    override fun onInputBox(prompt: String?) = delegate.handleInputBox(prompt)

    override fun onShowMenuOld() {
        val index = delegate.handleShowMenu(delegate.mutableMenuItemList.toList())
        selectMenuItem(index)
        delegate.mutableMenuItemList.clear()
    }

    override fun onAddMenuItem(name: String?, imgPath: String?) {
        delegate.mutableMenuItemList.add(LibGenItem(name.orEmpty(), imgPath.orEmpty()))
    }

    override fun onShowWindow(type: Int, isShow: Boolean) = delegate.handleShowWindow(type, isShow)
    @SuppressLint("Recycle")
    override fun onOpenGame(path: String?): Int {
        if (path.isNullOrBlank()) return super.onOpenGame(path)
        val targetFileUri =
            delegate.handleOpenGameStatus(path).getOrDefault("").toUri()
        if (targetFileUri == Uri.EMPTY) return super.onOpenGame(path)

        return try {
            context.contentResolver
                .openFileDescriptor(path.toUri(), "r")?.fd
                ?: super.onOpenGame(path)
        } catch (_: Exception) {
            super.onOpenGame(path)
        }
    }

    override fun onOpenGameStatus(filename: String?) =
        delegate.handleOpenGameStatus(filename, ::loadGameState)

    override fun onSaveGameStatus(filename: String?) =
        delegate.handleSaveGameStatus(filename, ::saveGameState)

    override fun onChangeQuestPath(path: String?) {
        if (!path.isNullOrBlank()) gameInterface.changeGameDir(path)
    }
}