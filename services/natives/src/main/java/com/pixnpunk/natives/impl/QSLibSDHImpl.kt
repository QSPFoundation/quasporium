@file:OptIn(ExperimentalContracts::class)

package com.pixnpunk.natives.impl

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.child
import com.anggrayudi.storage.file.getAbsolutePath
import com.libsdhqs.jni.QSLibSDH
import com.pixnpunk.dto.GameInterface
import com.pixnpunk.dto.LibGameState
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.LibIProxy
import com.pixnpunk.dto.LibReturnValue
import com.pixnpunk.dto.LibTypeDialog
import com.pixnpunk.dto.LibTypePopup
import com.pixnpunk.dto.LibTypeWindow
import com.pixnpunk.dto.LibUIConfig
import com.pixnpunk.natives.SupervisorViewModel.Companion.RECEIVE_FLOW_TIMEOUT
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
import kotlinx.coroutines.Runnable
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.Volatile
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.contracts.ExperimentalContracts

class QSLibSDHImpl(
    private val context: Context,
    override var gameInterface: GameInterface,
    override var gameState: LibGameState = LibGameState(),
    override var returnValueFuture: CompletableFuture<LibReturnValue> = CompletableFuture()
) : QSLibSDH(), LibIProxy {

    private val libLock = ReentrantLock()
    private var libThread: Thread? = null
    private val libQueue: ArrayBlockingQueue<Runnable> = ArrayBlockingQueue(1)
    @Volatile private lateinit var libHandler: Handler
    @Volatile private var libThreadInit = false
    @Volatile private var gameStartTime: Long = 0L
    @Volatile private var lastMsCountCallTime: Long = 0L
    private val currGameDir: DocumentFile?
        get() = fromUri(context, gameState.gameDirPath.toUri())

    private val mutableMenuItemList: MutableList<LibGenItem> = mutableListOf()

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
        val gameFileUri = gameState.gameFilePath.toUri()
        val gameFile = gameFileUri.toDocumentFile(context) ?: return false
        val gameFileFullPath = gameFile.getAbsolutePath(context)
        val gameFileDescriptor = context.contentResolver
            .openFileDescriptor(gameFileUri, "rw")?.detachFd() ?: return false
        return executeQspCommand { loadGameWorldFromFD(gameFileDescriptor, gameFileFullPath) }
    }

    private fun showLastQspError() {
        val errorData = getLastErrorData()
        gameInterface.showLibDialog(
            dialogType = LibTypeDialog.DIALOG_ERROR,
            inputString =
                """
                Location: ${errorData?.locName ?: ""}
                Action: ${errorData?.index}
                Line: ${errorData?.line}
                Error number: ${errorData?.errorNum}
                Description: ${
                    if (errorData != null) { getErrorDesc(errorData.errorNum) ?: "" } else { "" }
                }
                """.trimIndent()
        )
    }

    /**
     * Loads the interface configuration - using HTML, font and colors - from the library.
     *
     */
    private fun loadUIConfiguration() {
        val htmlResult = getVarValues("USEHTML", 0)
        val fSizeResult = getVarValues("FSIZE", 0)
        val bColorResult = getVarValues("BCOLOR", 0)
        val fColorResult = getVarValues("FCOLOR", 0)
        val lColorResult = getVarValues("LCOLOR", 0)

        gameInterface.setUIConfig(
            LibUIConfig(
                useHtml = htmlResult?.intValue != 0,
                fontSize = fSizeResult?.intValue ?: 0,
                backColor = bColorResult?.intValue ?: 0,
                fontColor = fColorResult?.intValue ?: 0,
                linkColor = lColorResult?.intValue ?: 0
            )
        )
    }

    private val actionsList: List<LibGenItem>
        get() {
            val gameDir = currGameDir
            if (!gameDir.isWritableDir()) return emptyList()

            val actions = mutableListOf<LibGenItem>()
            for (element in getActions() ?: return emptyList()) {
                if (element == null) continue
                var tempImagePath = element.image ?: ""
                var tempText = element.text ?: ""

                if (tempImagePath.isEmpty() && tempText.isEmpty()) continue

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
            for (element in getObjects() ?: return emptyList()) {
                if (element == null) continue
                var tempImagePath = element.image ?: ""
                var tempText = element.text ?: ""

                if (tempImagePath.isEmpty() && tempText.isEmpty()) continue

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

    // region LibQpProxy
    override fun startLibThread() {
        libThread = thread(name = "libNDKQSP") {
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
            executeQspCommand { setSelActionIndex(index, false) }
            executeQspCommand { executeSelActionCode(true) }
        }
    }

    override fun onObjectSelected(index: Int) {
        runOnQspThread { executeQspCommand { setSelObjectIndex(index, true) } }
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

    // endregion LibQpProxy
    // region LibQpCallbacks
    override fun onRefreshInt() {
        loadUIConfiguration()

        gameState = gameState.copy(
            mainDesc = getMainDesc() ?: "",
            varsDesc = getVarsDesc() ?: "",
            actionsList = actionsList,
            objectsList = objectsList
        )

        gameInterface.setGameState(gameState)
    }

    override fun onShowImage(path: String?) {
        if (!path.isNullOrBlank()) {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_PICTURE, path)
        }
    }

    override fun onSetTimer(msecs: Int) {
        gameInterface.setCountInter(msecs.toLong())
    }

    override fun onShowMessage(message: String?) {
        gameInterface.showLibDialog(
            dialogType = LibTypeDialog.DIALOG_MESSAGE,
            inputString = message ?: ""
        )
    }

    override fun onPlayFile(path: String?, volume: Int) {
        if (!path.isNullOrBlank()) {
            gameInterface.playFile(path, volume)
        }
    }

    override fun onIsPlayingFile(path: String?): Boolean {
        return if (path.isNullOrBlank()) {
            false
        } else {
            runCatching {
                CompletableFuture
                    .supplyAsync { gameInterface.isPlayingFile(path) }
                    .get(RECEIVE_FLOW_TIMEOUT, TimeUnit.MILLISECONDS)
            }.getOrDefault(false)
        }
    }

    override fun onCloseFile(path: String?) {
        if (path.isNullOrBlank()) {
            gameInterface.closeAllFiles()
        } else {
            gameInterface.closeFile(path)
        }
    }

    override fun onOpenGame(path: String?) {
        if (!path.isNullOrBlank()) {
            gameInterface.changeGameDir(path)
        }
    }

    override fun onOpenGameStatus(filename: String?) {
        if (filename.isNullOrBlank()) {
            gameInterface.showLibPopup(LibTypePopup.POPUP_LOAD)
        } else {
            runCatching {
                CompletableFuture
                    .supplyAsync { gameInterface.requestReceiveFile(filename) }
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

    override fun onSaveGameStatus(filename: String?) {
        if (filename.isNullOrBlank()) {
            gameInterface.showLibPopup(LibTypePopup.POPUP_SAVE)
        } else {
            runCatching {
                CompletableFuture
                    .supplyAsync { gameInterface.requestCreateFile(filename, MimeType.BINARY_FILE) }
                    .get(RECEIVE_FLOW_TIMEOUT, TimeUnit.MILLISECONDS)
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

    override fun onInputBox(prompt: String?): String {
        return runCatching {
            returnValueFuture = CompletableFuture()
            gameInterface.showLibDialog(
                dialogType = LibTypeDialog.DIALOG_INPUT_BOX,
                inputString = prompt ?: ""
            )
            returnValueFuture.get().dialogTextValue
        }.getOrDefault("")
    }

    override fun onGetMsCount(): Int {
        val now = SystemClock.elapsedRealtime()
        if (lastMsCountCallTime == 0L) {
            lastMsCountCallTime = gameStartTime
        }
        val dt = (now - lastMsCountCallTime).toInt()
        lastMsCountCallTime = now
        return dt
    }

    override fun onAddMenuItem(name: String?, imgPath: String?) {
        mutableMenuItemList.add(LibGenItem(name ?: "", imgPath ?: ""))
    }

    override fun onShowMenuOld() {
        selectMenuItem(
            index =
                runCatching {
                    returnValueFuture = CompletableFuture()
                    gameInterface.showLibDialog(
                        LibTypeDialog.DIALOG_MENU,
                        menuItems = mutableMenuItemList
                    )
                    returnValueFuture.get(RECEIVE_FLOW_TIMEOUT, TimeUnit.MILLISECONDS).dialogNumValue
                }.getOrDefault(-1)
        )
    }

    override fun onSleep(msecs: Int) {
        runCatching {
            Thread.sleep(msecs.toLong())
        }.onFailure { ex ->
            gameInterface.showLibDialog(
                LibTypeDialog.DIALOG_ERROR,
                ex.toString()
            )
        }
    }

    override fun onShowWindow(type: Int, isShow: Boolean) {
        gameInterface.changeVisWindow(
            when (type) {
                0 -> LibTypeWindow.ACTS
                1 -> LibTypeWindow.OBJS
                2 -> LibTypeWindow.VARS
                3 -> LibTypeWindow.INPUT
                else -> LibTypeWindow.MAIN
            }, isShow
        )
    }

    override fun onGetFileDesc(path: String?): Int {
        if (path.isNullOrBlank()) return super.onGetFileDesc(path)

        val targetFileUri = runCatching {
            CompletableFuture
                .supplyAsync { gameInterface.requestReceiveFile(path) }
                .get(RECEIVE_FLOW_TIMEOUT, TimeUnit.MILLISECONDS)
                .toUri()
        }.onFailure { println(it) }.getOrDefault(Uri.EMPTY)

        if (targetFileUri == Uri.EMPTY) return super.onGetFileDesc(path)
        return context.contentResolver
            .openFileDescriptor(targetFileUri, "r")?.detachFd() ?: super.onGetFileDesc(path)
    }
}