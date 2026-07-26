@file:OptIn(ExperimentalContracts::class)

package com.pixnpunk.game.data.impl

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import androidx.core.net.toUri
import com.anggrayudi.storage.extension.toDocumentFile
import com.libqsp.jni.QSPLib
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.game.data.mapper.toLibRawItem
import com.pixnpunk.game.domain.model.LibGameState
import com.pixnpunk.game.domain.model.LibReturnValue
import com.pixnpunk.game.domain.model.LibTypeDialog
import com.pixnpunk.game.domain.model.LibTypeWindow
import com.pixnpunk.game.domain.model.LibUIConfig
import com.pixnpunk.game.domain.repository.GameInterface
import com.pixnpunk.game.domain.repository.LibIProxy
import com.pixnpunk.utils.FileUtil.isWritableFile
import com.pixnpunk.utils.FileUtil.readFileContents
import com.pixnpunk.utils.FileUtil.writeFileContents
import kotlinx.coroutines.CompletableDeferred
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

    override var returnValueDeferred: CompletableDeferred<LibReturnValue>
        get() = delegate.returnValueDeferred
        set(value) { delegate.returnValueDeferred = value }

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

    override fun startLibThread() = delegate.startLibThread()
    override fun stopLibThread() = delegate.stopLibThread()

    override fun runGame(
        gameId: Long,
        gameTitle: String,
        gameDirPath: String,
        gameFilePath: String
    ) {
        delegate.runActionOnLibThread {
            doRunGame(gameId, gameTitle, gameDirPath.toUri(), gameFilePath.toUri())
        }
    }

    override fun restartGame() {
        delegate.runActionOnLibThread {
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
        delegate.executeOnLibThread {
            val gameData = path.toUri().readFileContents() ?: return@executeOnLibThread
            executeQspCommand { openSavedGameFromData(gameData, true) }
        }
    }

    override fun saveGameState(path: String) {
        delegate.executeOnLibThread {
            path.toUri().writeFileContents(
                saveGameAsData(false) ?: return@executeOnLibThread
            )
        }
    }

    override fun onActionClicked(index: Int) {
        delegate.runActionOnLibThread {
            executeQspCommand { setSelActIndex(index, false) }
            executeQspCommand { execSelAction(true) }
        }
    }

    override fun onObjectSelected(index: Int) {
        delegate.runActionOnLibThread { executeQspCommand { setSelObjIndex(index, true) } }
    }

    override fun onInputAreaClicked(code: String) {
        delegate.runActionOnLibThread {
            setInputStrText(code)
            executeQspCommand { execUserInput(true) }
        }
    }

    override fun execute(code: String) {
        delegate.runActionOnLibThread { executeQspCommand { execString(code, true) } }
    }

    override fun executeCounter() {
        delegate.runCounterOnLibThread { executeQspCommand { execCounter(true) } }
    }

    override fun onRefreshInt(isForced: Boolean, isNewDesc: Boolean) {
        loadUIConfiguration(isNewDesc)
        delegate.gameState = delegate.gameState.copy(
            mainDesc = mainDesc.orEmpty(),
            varsDesc = varsDesc.orEmpty(),
            actionsList = actionsList,
            objectsList = objectsList
        )
        gameInterface.setGameState(delegate.gameState)
    }

    override fun onShowWindow(type: Int, toShow: Boolean) =
        gameInterface.changeVisWindow(
            when {
                type and Window.ACTS != 0 -> LibTypeWindow.ACTS
                type and Window.OBJS != 0 -> LibTypeWindow.OBJS
                type and Window.VARS != 0 -> LibTypeWindow.VARS
                type and Window.INPUT != 0 -> LibTypeWindow.INPUT
                type and Window.VIEW != 0 -> LibTypeWindow.VIEW
                else -> LibTypeWindow.MAIN
            },
            toShow
        )

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

    override fun onOpenGame(file: String?, isNewGame: Boolean) {
        if (file.isNullOrBlank()) return

        val targetFileUri =
            delegate.handleOpenGameStatus(file).getOrDefault("").toUri()
        if (targetFileUri == Uri.EMPTY) return
        val targetFile = targetFileUri.toDocumentFile(context)
        if (!targetFile.isWritableFile()) return

        executeQspCommand {
            loadGameWorldFromData(targetFile.uri.readFileContents(), isNewGame)
        }
    }

    override fun onOpenGameStatus(file: String?) =
        delegate.handleOpenGameStatus(file, ::loadGameState)

    override fun onSaveGameStatus(file: String?) =
        delegate.handleSaveGameStatus(file, ::saveGameState)

    private fun loadUIConfiguration(isNewDesc: Boolean) =
        gameInterface.setUIConfig(
            LibUIConfig(
                isNoSave = getNumVarValue("NOSAVE", 0) != 0L,
                isUseHtml = getNumVarValue("USEHTML", 0) != 0L,
                isYesScrollMain = getNumVarValue("DISABLESCROLL", 0) == 0L && !isNewDesc,
                isYesScrollExtra = getNumVarValue("DISABLESCROLL", 0) == 0L,
                fontSize = getNumVarValue("FSIZE", 0).toInt(),
                backColor = getNumVarValue("BCOLOR", 0).toInt(),
                fontColor = getNumVarValue("FCOLOR", 0).toInt(),
                linkColor = getNumVarValue("LCOLOR", 0).toInt(),
            )
        )
}