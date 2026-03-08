@file:OptIn(ExperimentalContracts::class)

package com.pixnpunk.natives.impl

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import com.anggrayudi.storage.file.child
import com.pixnpunk.dto.GameInterface
import com.pixnpunk.dto.LibGameState
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.LibReturnValue
import com.pixnpunk.dto.LibTypeDialog
import com.pixnpunk.dto.LibTypeWindow
import com.pixnpunk.natives.dto.LibRawItem
import com.pixnpunk.utils.FileUtil.isWritableDir
import com.pixnpunk.utils.FileUtil.isWritableFile
import com.pixnpunk.utils.HtmlUtil.getSrcDir
import com.pixnpunk.utils.HtmlUtil.isContainsHtmlTags
import com.pixnpunk.utils.HtmlUtil.removeImgTags
import com.pixnpunk.utils.PathUtil.getFilename
import com.pixnpunk.utils.PathUtil.normalizeContentPath
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.contracts.ExperimentalContracts

class LibDelegate(
    private val context: Context,
    val gameInterface: GameInterface,
    private val initLib: () -> Unit,
    private val terminateLib: () -> Unit,
) {
    val libLock = ReentrantLock()
    private var libThread: Thread? = null
    val libQueue: ArrayBlockingQueue<Runnable> = ArrayBlockingQueue(1)

    @Volatile lateinit var libHandler: Handler
    @Volatile var libThreadInit = false
    @Volatile var gameStartTime: Long = 0L
    @Volatile var lastMsCountCallTime: Long = 0L

    var gameState: LibGameState = LibGameState()
    var returnValueFuture: CompletableFuture<LibReturnValue> = CompletableFuture()

    val currGameDir: DocumentFile?
        get() = fromUri(context, gameState.gameDirPath.toUri())

    val mutableMenuItemList: MutableList<LibGenItem> = mutableListOf()

    fun startLibThread(threadName: String) {
        libThread = thread(name = threadName) {
            initLib()
            Looper.prepare()
            libHandler = Handler(Looper.myLooper()!!)
            libThreadInit = true
            while (!libQueue.isEmpty()) {
                libQueue.poll()?.run()
            }
            Looper.loop()
            terminateLib()
        }
    }

    fun stopLibThread() {
        if (libThreadInit) {
            libHandler.looper.quitSafely()
            libThreadInit = false
        }
        libThread?.interrupt()
        libThread = null
    }

    fun runOnLibThread(runnable: Runnable) {
        if (libLock.isLocked) return

        if (libThreadInit) {
            returnValueFuture.cancel(true)
            libHandler.post {
                libLock.withLock { runnable.run() }
            }
        } else {
            libQueue.offer(runnable)
        }
    }

    fun buildGameItemsList(rawItems: List<LibRawItem>): List<LibGenItem> {
        val gameDir = currGameDir
        if (!gameDir.isWritableDir()) return emptyList()

        return rawItems
            .filter { it.text.isNotEmpty() || it.image.isNotEmpty() }
            .map { item ->
                var imagePath = resolveImagePath(gameDir, item.image)
                var text = item.text

                if (text.contains("<img")) {
                    imagePath = resolveImageFromHtml(gameDir, text, imagePath)
                    text = text.removeImgTags()
                }

                LibGenItem(text, imagePath)
            }
    }

    fun buildGameMenuList(menuItems: List<LibGenItem>): List<LibGenItem> {
        val gameDir = currGameDir
        if (!gameDir.isWritableDir()) return emptyList()

        return menuItems
            .filter { it.text.isNotEmpty() || it.imagePath.isNotEmpty() }
            .map {
                LibGenItem(
                    it.text,
                    gameDir.child(context, it.imagePath)?.uri.toString()
                )
            }
    }

    private fun resolveImagePath(gameDir: DocumentFile, imagePath: String): String {
        if (imagePath.isBlank()) return imagePath
        val normalizedPath = imagePath.getFilename().normalizeContentPath()
        val file = gameDir.child(context, normalizedPath)
        return if (file.isWritableFile()) file.uri.toString() else imagePath
    }

    private fun resolveImageFromHtml(
        gameDir: DocumentFile,
        text: String,
        fallbackImagePath: String
    ): String {
        val htmlPath = if (!text.isContainsHtmlTags()) text else text.getSrcDir()
        val file = gameDir.child(context, htmlPath)
        return if (file.isWritableFile()) file.uri.toString() else fallbackImagePath
    }

    fun handleShowImage(path: String?) {
        if (!path.isNullOrBlank()) {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_PICTURE, path)
        }
    }

    fun handleSetTimer(msecs: Int) {
        gameInterface.setCountInter(msecs.toLong())
    }

    fun handleShowMessage(message: String?) {
        runCatching {
            returnValueFuture = CompletableFuture()
            gameInterface.showLibDialog(
                dialogType = LibTypeDialog.DIALOG_MESSAGE,
                inputString = message.orEmpty()
            )
            returnValueFuture.get()
        }
    }

    fun handlePlayFile(path: String?, volume: Int) {
        if (!path.isNullOrBlank()) {
            gameInterface.playFile(path, volume)
        }
    }

    fun handleIsPlayingFile(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return runCatching {
            gameInterface.isPlayingFile(path)
        }.getOrDefault(false)
    }

    fun handleCloseFile(path: String?) {
        if (path.isNullOrBlank()) {
            gameInterface.closeAllFiles()
        } else {
            gameInterface.closeFile(path)
        }
    }

    fun handleSleep(msecs: Int) {
        runCatching {
            Thread.sleep(msecs.toLong())
        }.onFailure { ex ->
            gameInterface.showLibDialog(
                LibTypeDialog.DIALOG_ERROR,
                ex.toString()
            )
        }
    }

    fun handleShowWindow(type: Int, isShow: Boolean) {
        val windowType = when (type) {
            0 -> LibTypeWindow.ACTS
            1 -> LibTypeWindow.OBJS
            2 -> LibTypeWindow.VARS
            3 -> LibTypeWindow.INPUT
            else -> LibTypeWindow.MAIN
        }
        gameInterface.changeVisWindow(windowType, isShow)
    }

    fun handleGetMsCount(): Int {
        val now = SystemClock.elapsedRealtime()
        if (lastMsCountCallTime == 0L) lastMsCountCallTime = gameStartTime
        val dt = (now - lastMsCountCallTime).toInt()
        lastMsCountCallTime = now
        return dt
    }

    fun handleInputBox(prompt: String?): String {
        return runCatching {
            returnValueFuture = CompletableFuture()
            gameInterface.showLibDialog(
                dialogType = LibTypeDialog.DIALOG_INPUT_BOX,
                inputString = prompt.orEmpty()
            )
            returnValueFuture.get().dialogTextValue
        }.getOrDefault("")
    }

    fun handleShowMenu(items: List<LibGenItem>): Int =
        runCatching {
            returnValueFuture = CompletableFuture()
            gameInterface.showLibDialog(
                dialogType = LibTypeDialog.DIALOG_MENU,
                menuItems = buildGameMenuList(items)
            )
            returnValueFuture.get().dialogNumValue
        }.getOrDefault(-1)

    fun handleOpenGameStatus(
        filename: String?,
        loadGameState: (String) -> Unit
    ) {
        runCatching {
            returnValueFuture = CompletableFuture()
            if (filename.isNullOrBlank()) {
                gameInterface.requestReceiveFile()
            } else {
                gameInterface.requestReceiveFile(filename)
            }
            returnValueFuture.get().filePath
        }.onSuccess { path ->
            if (path.isNotEmpty()) {
                gameInterface.doWithCounterDisabled { loadGameState(path) }
            } else {
                gameInterface.showLibDialog(
                    LibTypeDialog.DIALOG_ERROR,
                    "Save file not found"
                )
            }
        }.onFailure { error ->
            val cause = (error as? java.util.concurrent.ExecutionException)?.cause ?: error
            if (cause !is GameInterface.UserCancelledException) {
                gameInterface.showLibDialog(
                    LibTypeDialog.DIALOG_ERROR,
                    error.message ?: "Unknown error"
                )
            }
        }
    }

    fun handleOpenGameStatus(
        filename: String
    ) = runCatching {
        returnValueFuture = CompletableFuture()
        gameInterface.requestReceiveFile(filename)
        returnValueFuture.get().filePath
    }.onFailure { error ->
        val cause = (error as? java.util.concurrent.ExecutionException)?.cause ?: error
        if (cause !is GameInterface.UserCancelledException) {
            gameInterface.showLibDialog(
                LibTypeDialog.DIALOG_ERROR,
                error.message ?: "Unknown error"
            )
        }
    }

    fun handleSaveGameStatus(
        filename: String?,
        saveGameState: (String) -> Unit
    ) {
        runCatching {
            returnValueFuture = CompletableFuture()
            if (filename.isNullOrBlank()) {
                gameInterface.requestCreateFile()
            } else {
                gameInterface.requestCreateFile(filename)
            }
            returnValueFuture.get().filePath
        }.onSuccess { path ->
            if (path.isNotEmpty()) {
                saveGameState(path)
            } else {
                gameInterface.showLibDialog(
                    LibTypeDialog.DIALOG_ERROR,
                    "Error access dir"
                )
            }
        }.onFailure { error ->
            val cause = (error as? java.util.concurrent.ExecutionException)?.cause ?: error
            if (cause !is GameInterface.UserCancelledException) {
                gameInterface.showLibDialog(
                    LibTypeDialog.DIALOG_ERROR,
                    error.message ?: "Unknown error"
                )
            }
        }
    }
}