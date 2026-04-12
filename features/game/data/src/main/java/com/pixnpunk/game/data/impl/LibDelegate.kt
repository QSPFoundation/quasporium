@file:OptIn(ExperimentalContracts::class)

package com.pixnpunk.game.data.impl

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import com.anggrayudi.storage.file.child
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.game.domain.model.LibGameState
import com.pixnpunk.game.domain.model.LibRawItem
import com.pixnpunk.game.domain.model.LibReturnValue
import com.pixnpunk.game.domain.model.LibTypeDialog
import com.pixnpunk.game.domain.model.LibTypeWindow
import com.pixnpunk.game.domain.repository.GameInterface
import com.pixnpunk.utils.FileUtil.isWritableDir
import com.pixnpunk.utils.FileUtil.isWritableFile
import com.pixnpunk.utils.HtmlUtil.getSrcDir
import com.pixnpunk.utils.HtmlUtil.isContainsHtmlTags
import com.pixnpunk.utils.HtmlUtil.removeImgTags
import com.pixnpunk.utils.PathUtil.getFilename
import com.pixnpunk.utils.PathUtil.normalizeContentPath
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
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
    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val libDispatcher = newSingleThreadContext("NativeLibThread")
    @OptIn(ExperimentalCoroutinesApi::class)
    private val libScope = CoroutineScope(libDispatcher + SupervisorJob())
    @Volatile private var nativeThread: Thread? = null

    private val libMutex = Mutex()
    private val libChannel = Channel<Runnable>(Channel.UNLIMITED)

    @Volatile var libThreadInit = false
    @Volatile var gameStartTime: Long = 0L
    @Volatile var lastMsCountCallTime: Long = 0L

    var gameState: LibGameState = LibGameState()
    var returnValueDeferred = CompletableDeferred<LibReturnValue>()

    val currGameDir: DocumentFile?
        get() = fromUri(context, gameState.gameDirPath.toUri())

    val mutableMenuItemList: MutableList<LibGenItem> = mutableListOf()

    fun startLibThread() {
        libScope.launch {
            nativeThread = Thread.currentThread()
            initLib()
            libThreadInit = true

            for (task in libChannel) {
                libMutex.withLock { task.run() }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun stopLibThread() {
        libThreadInit = false
        libChannel.close()
        libScope.launch {
            terminateLib()
            libScope.cancel()
            libDispatcher.close()
        }
    }

    fun executeOnLibThread(block: () -> Unit) {
        if (Thread.currentThread() == nativeThread) {
            block()
        } else {
            if (libThreadInit) {
                returnValueDeferred.cancel()
                libScope.launch { block() }
            } else {
                libChannel.trySend(Runnable { block() })
            }
        }
    }

    fun runActionOnLibThread(runnable: Runnable) {
        if (!libThreadInit) {
            libChannel.trySend(runnable)
        } else {
            returnValueDeferred.cancel()
            libScope.launch { libMutex.withLock { runnable.run() } }
        }
    }

    fun runCounterOnLibThread(runnable: Runnable) {
        if (!libThreadInit || libMutex.isLocked) return
        libScope.launch {
            if (libMutex.tryLock()) {
                try {
                    runnable.run()
                }
                finally {
                    libMutex.unlock()
                }
            }
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
            returnValueDeferred = CompletableDeferred()
            gameInterface.showLibDialog(
                dialogType = LibTypeDialog.DIALOG_MESSAGE,
                inputString = message.orEmpty()
            )
            runBlocking { returnValueDeferred.await() }
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
            returnValueDeferred = CompletableDeferred()
            gameInterface.showLibDialog(
                dialogType = LibTypeDialog.DIALOG_INPUT_BOX,
                inputString = prompt.orEmpty()
            )
            runBlocking { returnValueDeferred.await() }.dialogTextValue
        }.getOrDefault("")
    }

    fun handleShowMenu(items: List<LibGenItem>): Int =
        runCatching {
            returnValueDeferred = CompletableDeferred()
            gameInterface.showLibDialog(
                dialogType = LibTypeDialog.DIALOG_MENU,
                menuItems = buildGameMenuList(items)
            )
            runBlocking { returnValueDeferred.await() }.dialogNumValue
        }.getOrDefault(-1)

    fun handleOpenGameStatus(
        filename: String?,
        loadGameState: (String) -> Unit
    ) {
        runCatching {
            returnValueDeferred = CompletableDeferred()
            if (filename.isNullOrBlank()) {
                gameInterface.requestReceiveFile()
            } else {
                gameInterface.requestReceiveFile(filename)
            }
            runBlocking { returnValueDeferred.await() }.filePath
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
            val cause = (error as? ExecutionException)?.cause ?: error
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
        returnValueDeferred = CompletableDeferred()
        gameInterface.requestReceiveFile(filename)
        runBlocking { returnValueDeferred.await() }.filePath
    }.onFailure { error ->
        val cause = (error as? ExecutionException)?.cause ?: error
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
            returnValueDeferred = CompletableDeferred()
            if (filename.isNullOrBlank()) {
                gameInterface.requestCreateFile()
            } else {
                gameInterface.requestCreateFile(filename)
            }
            runBlocking { returnValueDeferred.await() }.filePath
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
            val cause = (error as? ExecutionException)?.cause ?: error
            if (cause !is GameInterface.UserCancelledException) {
                gameInterface.showLibDialog(
                    LibTypeDialog.DIALOG_ERROR,
                    error.message ?: "Unknown error"
                )
            }
        }
    }
}