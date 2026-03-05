@file:OptIn(ExperimentalContracts::class)

package com.pixnpunk.natives.impl

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import androidx.core.net.toUri
import com.libqsp.jni.QSPLib
import com.pixnpunk.dto.GameInterface
import com.pixnpunk.dto.LibGameState
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.LibIProxy
import com.pixnpunk.dto.LibReturnValue
import com.pixnpunk.dto.LibTypeDialog
import com.pixnpunk.dto.LibTypeWindow
import com.pixnpunk.dto.LibUIConfig
import com.pixnpunk.natives.dto.toLibRawItem
import com.pixnpunk.utils.FileUtil.readFileContents
import com.pixnpunk.utils.FileUtil.writeFileContents
import com.pixnpunk.utils.ThreadUtil.isSameThread
import java.util.concurrent.CompletableFuture
import kotlin.contracts.ExperimentalContracts

class QSPLibImpl(
    private val context: Context,
    override var gameInterface: GameInterface
) : QSPLib(), LibIProxy {

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

    private inline fun executeQspCommand(block: () -> Boolean): Boolean {
        return if (block()) true else {
            showLastLibError()
            false
        }
    }

    private fun showLastLibError() {
        val errorData = lastErrorData
        gameInterface.showLibDialog(
            dialogType = LibTypeDialog.DIALOG_ERROR,
            inputString = """
                Location: ${errorData.locName.orEmpty()}
                Action: ${errorData.actIndex}
                Line: ${errorData.intLineNum}
                Error number: ${errorData.errorNum}
                Description: ${getErrorDesc(errorData.errorNum).orEmpty()}
            """.trimIndent()
        )
    }

    private fun loadGameWorld(): Boolean {
        val gameData = delegate.gameState.gameFilePath.toUri().readFileContents() ?: return false
        return executeQspCommand { loadGameWorldFromData(gameData, true) }
    }

    private val actionsList: List<LibGenItem>
        get() = delegate.buildGameItemsList(
            getActions().orEmpty().filterNotNull().map { it.toLibRawItem() }
        )

    private val objectsList: List<LibGenItem>
        get() = delegate.buildGameItemsList(
            getObjects().orEmpty().filterNotNull().map { it.toLibRawItem() }
        )

    override fun startLibThread() = delegate.startLibThread("libQSP")
    override fun stopLibThread() = delegate.stopLibThread()

    override fun runGame(gameId: Long, gameTitle: String, gameDirPath: String, gameFilePath: String) {
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
        val gameData = path.toUri().readFileContents() ?: return
        executeQspCommand { openSavedGameFromData(gameData, true) }
    }

    override fun saveGameState(path: String) {
        if (!isSameThread(delegate.libHandler.looper.thread)) {
            delegate.runOnLibThread { saveGameState(path) }
            return
        }
        path.toUri().writeFileContents(saveGameAsData(false) ?: return)
    }

    override fun onActionClicked(index: Int) {
        delegate.runOnLibThread {
            executeQspCommand { setSelActIndex(index, false) }
            executeQspCommand { execSelAction(true) }
        }
    }

    override fun onObjectSelected(index: Int) {
        delegate.runOnLibThread { executeQspCommand { setSelObjIndex(index, true) } }
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

    override fun onRefreshInt(isForced: Boolean, isNewDesc: Boolean) {
        loadUIConfiguration()
        delegate.gameState = delegate.gameState.copy(
            mainDesc = mainDesc.orEmpty(),
            varsDesc = varsDesc.orEmpty(),
            actionsList = actionsList,
            objectsList = objectsList
        )
        gameInterface.setGameState(delegate.gameState)
    }

    override fun onShowWindow(type: Int, toShow: Boolean) {
        val windowType = when {
            type and Window.ACTS != 0 -> LibTypeWindow.ACTS
            type and Window.OBJS != 0 -> LibTypeWindow.OBJS
            type and Window.VARS != 0 -> LibTypeWindow.VARS
            type and Window.INPUT != 0 -> LibTypeWindow.INPUT
            type and Window.VIEW != 0 -> LibTypeWindow.VIEW
            else -> LibTypeWindow.MAIN
        }
        gameInterface.changeVisWindow(windowType, toShow)
    }

    override fun onShowImage(file: String?) = delegate.handleShowImage(file)
    override fun onSetTimer(msecs: Int) = delegate.handleSetTimer(msecs)
    override fun onShowMessage(text: String) = delegate.handleShowMessage(text)
    override fun onPlayFile(file: String?, volume: Int) = delegate.handlePlayFile(file, volume)
    override fun onIsPlayingFile(file: String?) = delegate.handleIsPlayingFile(file)
    override fun onCloseFile(file: String?) = delegate.handleCloseFile(file)
    override fun onSleep(msecs: Int) = delegate.handleSleep(msecs)
    override fun onGetMsCount() = delegate.handleGetMsCount()
    override fun onInputBox(text: String) = delegate.handleInputBox(text)

    override fun onShowMenu(items: Array<ListItem>): Int {
        return delegate.handleShowMenu(
            items.map { LibGenItem(it.name.orEmpty(), it.image.orEmpty()) }
        )
    }

    override fun onOpenGameStatus(file: String?) =
        delegate.handleOpenGameStatus(file, ::loadGameState)

    override fun onSaveGameStatus(file: String?) =
        delegate.handleSaveGameStatus(file, ::saveGameState)

    override fun onOpenGame(file: String?, isNewGame: Boolean) {
        if (!file.isNullOrBlank()) gameInterface.changeGameDir(file)
    }

    private fun loadUIConfiguration() {
        gameInterface.setUIConfig(
            LibUIConfig(
                useHtml = getNumVarValue("USEHTML", 0) != 0L,
                fontSize = getNumVarValue("FSIZE", 0).toInt(),
                backColor = getNumVarValue("BCOLOR", 0).toInt(),
                fontColor = getNumVarValue("FCOLOR", 0).toInt(),
                linkColor = getNumVarValue("LCOLOR", 0).toInt()
            )
        )
    }
}