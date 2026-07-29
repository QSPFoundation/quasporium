package com.pixnpunk.game.data.engine.jni

import android.content.Context
import androidx.core.net.toUri
import com.libqsplegacy.jni.QSPLegacyLib
import com.pixnpunk.game.data.model.RawItem
import com.pixnpunk.game.data.vfs.VirtualFile
import com.pixnpunk.game.data.vfs.impl.DirectVirtualFile
import com.pixnpunk.game.data.vfs.impl.DocumentVirtualFile
import com.pixnpunk.game.domain.model.LibUIConfig
import com.pixnpunk.game.domain.model.WindowType

internal class QspLegacyEngineBridgeImpl(
    private val context: Context
) : QSPLegacyLib(), NativeEngineBridge {

    private var engineListener: NativeEngineListener? = null

    private var gameStartTime: Long = 0L
    private var lastMsCountCallTime: Long = 0L

    override fun initializeEngine() = init()

    override fun destroyEngine() = terminate()

    override fun setListener(listener: NativeEngineListener) {
        this.engineListener = listener
    }

    override fun executeAction(index: Int): Boolean {
        executeCommand { setSelActionIndex(index, false) }
        return executeCommand { executeSelActionCode(true) }
    }

    override fun executeCounter(): Boolean =
        executeCommand { execCounter(true) }

    override fun executeString(input: String): Boolean =
        executeCommand { execString(input, true) }

    private inline fun executeCommand(block: () -> Boolean): Boolean {
        val isSuccess = block()
        if (!isSuccess) {
            val errorData = lastErrorData ?: return false
            val desc = getErrorDesc(errorData.errorNum) ?: "Unknown error"

            val errorMsg = """
                Location: ${errorData.locName ?: ""}
                Action: ${errorData.index}
                Line: ${errorData.line}
                Error number: ${errorData.errorNum}
                Description: $desc
            """.trimIndent()

            engineListener?.onError(errorMsg)
        }
        return isSuccess
    }

    override suspend fun loadGame(
        file: VirtualFile,
        isNewGame: Boolean
    ): Boolean =
        when (file) {
            is DirectVirtualFile ->
                executeCommand { loadGameWorldFromPath(file.path.value, isNewGame) }

            is DocumentVirtualFile ->
                context.contentResolver.openFileDescriptor(
                    file.path.value.toUri(),
                    "r"
                )?.use { pfd ->
                    executeCommand { loadGameWorldFromFD(pfd.fd, isNewGame) }
                } ?: false

            else ->
                file.readBytes().getOrNull().let { data ->
                    executeCommand { loadGameWorldFromData(data, isNewGame) }
                }
        }

    override suspend fun loadGameState(
        file: VirtualFile
    ): Boolean =
        when (file) {
            is DirectVirtualFile ->
                executeCommand { openSavedGameFromPath(file.path.value, true) }

            is DocumentVirtualFile ->
                context.contentResolver.openFileDescriptor(
                    file.path.value.toUri(),
                    "r"
                )?.use { pfd ->
                    executeCommand { openSavedGameFromFD(pfd.fd, true) }
                } ?: false

            else ->
                file.readBytes().getOrNull().let { data ->
                    executeCommand { openSavedGameFromData(data, true) }
                }
        }

    override suspend fun saveGameState(
        file: VirtualFile
    ): Boolean =
        when (file) {
            is DirectVirtualFile ->
                saveGameByPath(file.path.value, false)

            is DocumentVirtualFile ->
                context.contentResolver.openFileDescriptor(
                    file.path.value.toUri(),
                    "w"
                )?.use { pfd ->
                    saveGameByFD(pfd.fd, false)
                } ?: false

            else ->
                saveGameAsData(false).let { data ->
                    file.writeBytes(data).isSuccess
                }
        }

    override fun restartGame(): Boolean =
        executeCommand {
            gameStartTime = System.nanoTime()
            lastMsCountCallTime = 0L

            restartGame(true)
        }

    override fun selectObject(index: Int): Boolean =
        executeCommand { setSelObjectIndex(index, true) }

    override fun onInputAreaClicked(text: String) {
        setInputStrText(text)
        executeCommand { execUserInput(true) }
    }

    private fun loadUIConfiguration() =
        LibUIConfig(
            isNoSave = getVarValues("NOSAVE", 0)?.intValue != 0,
            isUseHtml = getVarValues("USEHTML", 0)?.intValue != 0,
            isYesScrollMain = getVarValues("DISABLESCROLL", 0)?.intValue == 0,
            isYesScrollExtra = getVarValues("DISABLESCROLL", 0)?.intValue == 0,
            fontSize = getVarValues("FSIZE", 0)?.intValue ?: 0,
            backColor = getVarValues("BCOLOR", 0)?.intValue ?: 0,
            fontColor = getVarValues("FCOLOR", 0)?.intValue ?: 0,
            linkColor = getVarValues("LCOLOR", 0)?.intValue ?: 0
        )

    override fun onRefreshInt() {
        val listener = engineListener ?: return

        val mainDesc = mainDesc ?: ""
        val varsDesc = varsDesc ?: ""
        val actions = actions?.mapNotNull { it.toRawItem() }.orEmpty()
        val objects = objects?.mapNotNull { it.toRawItem() }.orEmpty()

        listener.onStateRefreshed(
            mainDesc,
            varsDesc,
            loadUIConfiguration(),
            actions,
            objects
        )
    }

    override fun onSetTimer(msecs: Int) {
        engineListener?.onSetTimer(msecs)
    }

    override fun onGetMsCount(): Int {
        val now = System.nanoTime()
        if (lastMsCountCallTime == 0L) {
            lastMsCountCallTime = gameStartTime
        }
        val dt = (now - lastMsCountCallTime) / 1_000_000L
        lastMsCountCallTime = now
        return dt.toInt()
    }

    override fun onSleep(msecs: Int) {
        if (msecs <= 0) return
        try {
            Thread.sleep(msecs.toLong())
        } catch (e: InterruptedException) {
            engineListener?.onError("Sleep interrupted: ${e.message}")
        }
    }

    override fun onShowMessage(message: String?) {
        val safeMessage = message ?: return
        engineListener?.onShowMessage(safeMessage)
    }

    override fun onShowImage(path: String?) {
        if (!path.isNullOrBlank()) {
            engineListener?.onShowImage(path)
        }
    }

    override fun onInputBox(prompt: String?): String {
        return engineListener?.onInputBox(prompt ?: "") ?: ""
    }

    override fun onShowMenu(items: Array<out ListItem>?): Int {
        if (items.isNullOrEmpty()) return -1
        val mappedItems = items.mapNotNull { it.toRawItem() }
        return engineListener?.onShowMenu(mappedItems) ?: -1
    }

    override fun onShowWindow(type: Int, isShow: Boolean) {
        val windowType = mapSdhWindowType(type) ?: return
        engineListener?.onShowWindow(windowType, isShow)
    }

    override fun onOpenGame(file: String?, isNewGame: Boolean) {
        val safePath = file ?: return
        engineListener?.onOpenGame(safePath, isNewGame)
    }

    override fun onOpenGameStatus(filename: String?) {
        val safePath = filename ?: return
        engineListener?.onLoadSave(safePath)
    }

    override fun onSaveGameStatus(filename: String?) {
        val safePath = filename ?: return
        engineListener?.onSaveGame(safePath)
    }

    override fun onCloseFile(path: String?) {
        engineListener?.onCloseAudio(path)
    }

    override fun onIsPlayingFile(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return engineListener?.onCheckAudioPlaying(path) ?: false
    }

    override fun onPlayFile(path: String?, volume: Int) {
        if (!path.isNullOrBlank()) {
            engineListener?.onPlayAudio(path, volume)
        }
    }

    private fun mapSdhWindowType(type: Int): WindowType? = when (type) {
        0 -> WindowType.ACTS
        1 -> WindowType.OBJS
        2 -> WindowType.VARS
        3 -> WindowType.INPUT
        else -> WindowType.MAIN
    }

    private fun ListItem?.toRawItem(): RawItem? {
        if (this == null) return null
        val safeText = text() ?: return null
        val safeImage = image() ?: ""
        return RawItem(safeText, safeImage)
    }
}