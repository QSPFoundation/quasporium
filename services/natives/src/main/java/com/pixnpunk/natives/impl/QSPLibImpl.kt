@file:OptIn(ExperimentalContracts::class)

package com.pixnpunk.natives.impl

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.child
import com.libqsp.jni.QSPLib
import com.pixnpunk.dto.GameInterface
import com.pixnpunk.dto.LibGameState
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.LibIProxy
import com.pixnpunk.dto.LibReturnValue
import com.pixnpunk.dto.LibTypeDialog
import com.pixnpunk.dto.LibTypePopup
import com.pixnpunk.dto.LibTypeWindow
import com.pixnpunk.dto.LibUIConfig
import com.pixnpunk.natives.NativeViewModel.Companion.RECEIVE_FLOW_TIMEOUT
import com.pixnpunk.utils.FileUtil.isWritableDir
import com.pixnpunk.utils.FileUtil.isWritableFile
import com.pixnpunk.utils.FileUtil.readFileContents
import com.pixnpunk.utils.FileUtil.writeFileContents
import com.pixnpunk.utils.HtmlUtil.getSrcDir
import com.pixnpunk.utils.HtmlUtil.isContainsHtmlTags
import com.pixnpunk.utils.HtmlUtil.removeImgTags
import com.pixnpunk.utils.PathUtil.getFilename
import com.pixnpunk.utils.PathUtil.normalizeContentPath
import com.pixnpunk.utils.ThreadUtil.isSameThread
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.Volatile
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.contracts.ExperimentalContracts

class QSPLibImpl(
    private val context: Context,
    override var gameInterface: GameInterface,
    override var gameState: LibGameState = LibGameState(),
    override var returnValueFuture: CompletableFuture<LibReturnValue> = CompletableFuture()
) : QSPLib(), LibIProxy {

    private val libLock = ReentrantLock()
    private var libThread: Thread? = null
    private val libQueue: ArrayBlockingQueue<Runnable> = ArrayBlockingQueue(1)
    @Volatile private lateinit var libHandler: Handler
    @Volatile private var libThreadInit = false
    @Volatile private var gameStartTime: Long = 0L
    @Volatile private var lastMsCountCallTime: Long = 0L
    private val currGameDir: DocumentFile?
        get() = fromUri(context, gameState.gameDirPath.toUri())

    private inline fun executeQspCommand(block: () -> Boolean): Boolean {
        return if (block()) { true } else { showLastQspError(); false }
    }

    private fun runOnQspThread(runnable: Runnable) {
        if (libThreadInit) {
            libHandler.post {
                libLock.withLock { runnable.run() }
            }
        } else {
            libQueue.offer(runnable)
        }
    }

    private fun loadGameWorld(): Boolean {
        val gameData = gameState.gameFilePath.toUri().readFileContents() ?: return false
        return executeQspCommand { loadGameWorldFromData(gameData, true) }
    }

    private fun showLastQspError() {
        val errorData = lastErrorData
        gameInterface.showLibDialog(
            dialogType = LibTypeDialog.DIALOG_ERROR,
            inputString =
                """
                Location: ${errorData.locName ?: ""}
                Action: ${errorData.actIndex}
                Line: ${errorData.intLineNum}
                Error number: ${errorData.errorNum}
                Description: ${getErrorDesc(errorData.errorNum) ?: ""}
                """.trimIndent()
        )
    }

    /**
     * Loads the interface configuration - using HTML, font and colors - from the library.
     *
     */
    private fun loadUIConfiguration() {
        val htmlResult = getNumVarValue("USEHTML", 0)
        val fSizeResult = getNumVarValue("FSIZE", 0)
        val bColorResult = getNumVarValue("BCOLOR", 0)
        val fColorResult = getNumVarValue("FCOLOR", 0)
        val lColorResult = getNumVarValue("LCOLOR", 0)

        gameInterface.setUIConfig(
            LibUIConfig(
                useHtml = htmlResult != 0L,
                fontSize = fSizeResult.toInt(),
                backColor = bColorResult.toInt(),
                fontColor = fColorResult.toInt(),
                linkColor = lColorResult.toInt()
            )
        )
    }

    private val actionsList: List<LibGenItem>
        get() {
            val gameDir = currGameDir
            if (!gameDir.isWritableDir()) return emptyList()

            val actions = mutableListOf<LibGenItem>()
            for (element in getActions()) {
                if (element == null) continue
                var tempImagePath = element.image ?: ""
                var tempText = element.name ?: ""

                if (tempImagePath.isNotBlank()) {
                    val tempPath = tempImagePath.getFilename().normalizeContentPath()
                    val fileFromPath = gameDir.child(context, tempPath)
                    if (fileFromPath.isWritableFile()) {
                        tempImagePath = fileFromPath.uri.toString()
                    }
                }

                if (tempText.contains("<img")) {
                    if (!tempText.isContainsHtmlTags()) {
                        val fileFromPath = gameDir.child(context, tempText)
                        if (fileFromPath.isWritableFile()) {
                            tempImagePath = fileFromPath.uri.toString()
                        }
                    } else {
                        val tempPath = tempText.getSrcDir()
                        val fileFromPath = gameDir.child(context, tempPath)
                        if (fileFromPath.isWritableFile()) {
                            tempImagePath = fileFromPath.uri.toString()
                        }
                    }
                    tempText = tempText.removeImgTags()
                }

                actions.add(LibGenItem(tempText, tempImagePath))
            }

            return actions
        }

    private val objectsList: List<LibGenItem>
        get() {
            val gameDir = currGameDir
            if (!gameDir.isWritableDir()) return emptyList()

            val objects = mutableListOf<LibGenItem>()
            for (element in getObjects()) {
                if (element == null) continue
                var tempImagePath = element.image ?: ""
                var tempText = element.name ?: ""

                if (tempImagePath.isNotBlank()) {
                    val tempPath = tempImagePath.getFilename().normalizeContentPath()
                    val fileFromPath = gameDir.child(context, tempPath)
                    if (fileFromPath.isWritableFile()) {
                        tempImagePath = fileFromPath.uri.toString()
                    }
                }

                if (tempText.contains("<img")) {
                    if (!tempText.isContainsHtmlTags()) {
                        val fileFromPath = gameDir.child(context, tempText)
                        if (fileFromPath.isWritableFile()) {
                            tempImagePath = fileFromPath.uri.toString()
                        }
                    } else {
                        val tempPath = tempText.getSrcDir()
                        val fileFromPath = gameDir.child(context, tempPath)
                        if (fileFromPath.isWritableFile()) {
                            tempImagePath = fileFromPath.uri.toString()
                        }
                    }
                    tempText = tempText.removeImgTags()
                }

                objects.add(LibGenItem(tempText, tempImagePath))
            }

            return objects
        }

    override fun startLibThread() {
        libThread = thread(name = "libQSP") {
            init()
            Looper.prepare()
            libHandler = Handler(Looper.myLooper()!!)
            libThreadInit = true
            while (!libQueue.isEmpty()) {
                libQueue.poll()?.run()
            }
            Looper.loop()
            terminate()
        }
    }

    override fun stopLibThread() {
        if (libThreadInit) {
            val handler = libHandler
            handler.looper.quitSafely()
            libThreadInit = false
        }

        libThread?.interrupt()
    }

    override fun enableDebugMode(isDebug: Boolean) {
        runOnQspThread { enableDebugMode(isDebug) }
    }

    override fun runGame(
        gameId: Long,
        gameTitle: String,
        gameDirPath: String,
        gameFilePath: String
    ) {
        runOnQspThread {
            doRunGame(
                gameId, gameTitle, gameDirPath.toUri(), gameFilePath.toUri()
            )
        }
    }

    override fun restartGame() {
        runOnQspThread {
            doRunGame(
                gameState.gameId,
                gameState.gameTitle,
                gameState.gameDirPath.toUri(),
                gameState.gameFilePath.toUri()
            )
        }
    }

    private fun doRunGame(id: Long, title: String, dir: Uri, file: Uri) {
        gameInterface.doWithCounterDisabled {
            gameInterface.closeAllFiles()
            gameState = gameState.copy(
                gameRunning = true,
                gameId = id,
                gameTitle = title,
                gameDirPath = dir.toString(),
                gameFilePath = file.toString()
            )
            if (!loadGameWorld()) return@doWithCounterDisabled
            gameStartTime = SystemClock.elapsedRealtime()
            lastMsCountCallTime = 0
            executeQspCommand { restartGame(true) }
        }
    }

    override fun loadGameState(path: String) {
        if (!isSameThread(libHandler.looper.thread)) {
            runOnQspThread { loadGameState(path) }
            return
        }

        val gameData = path.toUri().readFileContents() ?: return
        executeQspCommand { openSavedGameFromData(gameData, true) }
    }

    override fun saveGameState(path: String) {
        if (!isSameThread(libHandler.looper.thread)) {
            runOnQspThread { saveGameState(path) }
            return
        }

        path.toUri().writeFileContents(
            dataToWrite = saveGameAsData(false) ?: return
        )
    }

    override fun onActionClicked(index: Int) {
        runOnQspThread {
            executeQspCommand { setSelActIndex(index, false) }
            executeQspCommand { execSelAction(true) }
        }
    }

    override fun onObjectSelected(index: Int) {
        runOnQspThread { executeQspCommand { setSelObjIndex(index, true) } }
    }

    override fun onInputAreaClicked(code: String) {
        runOnQspThread {
            setInputStrText(code)
            executeQspCommand { execUserInput(true) }
        }
    }

    override fun execute(code: String) {
        runOnQspThread { executeQspCommand { execString(code, true) } }
    }

    override fun executeCounter() {
        if (libLock.isLocked) return
        runOnQspThread { executeQspCommand { execCounter(true) } }
    }

    override fun onRefreshInt(isForced: Boolean, isNewDesc: Boolean) {
        loadUIConfiguration()

        gameState = gameState.copy(
            mainDesc = mainDesc,
            varsDesc = varsDesc,
            actionsList = actionsList,
            objectsList = objectsList
        )

        gameInterface.setGameState(gameState)
    }

    override fun onShowImage(file: String?) {
        if (!file.isNullOrBlank()) {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_PICTURE, file)
        }
    }

    override fun onSetTimer(msecs: Int) {
        gameInterface.setCountInter(msecs.toLong())
    }

    override fun onShowMessage(text: String) {
        gameInterface.showLibDialog(LibTypeDialog.DIALOG_MESSAGE, text)
    }

    override fun onPlayFile(file: String?, volume: Int) {
        if (!file.isNullOrBlank()) {
            gameInterface.playFile(file, volume)
        }
    }

    override fun onIsPlayingFile(file: String?): Boolean {
        return if (file.isNullOrBlank()) {
            false
        } else {
            runCatching {
                CompletableFuture
                    .supplyAsync { gameInterface.isPlayingFile(file) }
                    .get(RECEIVE_FLOW_TIMEOUT, TimeUnit.MILLISECONDS)
            }.getOrDefault(false)
        }
    }

    override fun onCloseFile(file: String?) {
        if (file.isNullOrBlank()) {
            gameInterface.closeAllFiles()
        } else {
            gameInterface.closeFile(file)
        }
    }

    override fun onOpenGameStatus(file: String?) {
        if (file.isNullOrEmpty()) {
            gameInterface.showLibPopup(LibTypePopup.POPUP_LOAD)
        } else {
            gameInterface.requestReceiveFile(file)
            runCatching {
                CompletableFuture
                    .supplyAsync { gameInterface.requestReceiveFile(file) }
                    .get(RECEIVE_FLOW_TIMEOUT, TimeUnit.MILLISECONDS)
            }.onSuccess {
                if (it.isNotEmpty()) {
                    gameInterface.doWithCounterDisabled { loadGameState(it) }
                } else {
                    gameInterface.showLibDialog(
                        LibTypeDialog.DIALOG_ERROR,
                        "Save file not found"
                    )
                }
            }
        }
    }

    override fun onSaveGameStatus(file: String?) {
        if (file.isNullOrEmpty()) {
            gameInterface.showLibPopup(LibTypePopup.POPUP_SAVE)
        } else {
            gameInterface.requestCreateFile(file, MimeType.BINARY_FILE)
            runCatching {
                returnValueFuture = CompletableFuture()
                gameInterface.requestCreateFile(file, MimeType.BINARY_FILE)
                returnValueFuture.get(RECEIVE_FLOW_TIMEOUT, TimeUnit.MILLISECONDS).filePath
            }.onSuccess {
                if (it.isNotEmpty()) {
                    saveGameState(it)
                } else {
                    gameInterface.showLibDialog(
                        LibTypeDialog.DIALOG_ERROR,
                        "Error access dir"
                    )
                }
            }
        }
    }

    override fun onInputBox(text: String): String {
        return runCatching {
            returnValueFuture = CompletableFuture()
            gameInterface.showLibDialog(
                dialogType = LibTypeDialog.DIALOG_INPUT_BOX,
                inputString = text
            )
            returnValueFuture.get().dialogTextValue
        }.getOrDefault("")
    }

    override fun onGetMsCount(): Int {
        val now = SystemClock.elapsedRealtime()
        if (lastMsCountCallTime == 0L) {
            lastMsCountCallTime = gameStartTime
        }
        val dt = now - lastMsCountCallTime
        lastMsCountCallTime = now
        return dt.toInt()
    }

    override fun onShowMenu(items: Array<ListItem>): Int {
        return runCatching {
            returnValueFuture = CompletableFuture()
            gameInterface.showLibDialog(
                LibTypeDialog.DIALOG_MENU,
                menuItems = items.map { LibGenItem(it.name ?: "", it.image ?: "") }
            )
            returnValueFuture.get(RECEIVE_FLOW_TIMEOUT, TimeUnit.MILLISECONDS).dialogNumValue
        }.getOrElse { super.onShowMenu(items) }
    }

    override fun onSleep(msecs: Int) {
        try {
            Thread.sleep(msecs.toLong())
        } catch (ex: InterruptedException) {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, ex.toString())
        }
    }

    override fun onShowWindow(type: Int, toShow: Boolean) {
        gameInterface.changeVisWindow(
            when (type) {
                Window.MAIN -> LibTypeWindow.MAIN
                Window.VARS -> LibTypeWindow.VARS
                Window.ACTS -> LibTypeWindow.ACTS
                Window.OBJS -> LibTypeWindow.OBJS
                Window.INPUT -> LibTypeWindow.INPUT
                Window.VIEW -> LibTypeWindow.VIEW
                else -> LibTypeWindow.MAIN
            }, toShow
        )
    }

    override fun onOpenGame(file: String?, isNewGame: Boolean) {
        if (!file.isNullOrBlank()) {
            gameInterface.changeGameDir(file)
        }
    }
}