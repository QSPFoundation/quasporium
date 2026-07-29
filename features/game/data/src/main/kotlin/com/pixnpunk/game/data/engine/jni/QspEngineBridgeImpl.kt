package com.pixnpunk.game.data.engine.jni

import com.libqsp.jni.QSPLib
import com.pixnpunk.game.data.model.RawItem
import com.pixnpunk.game.data.vfs.VirtualFile
import com.pixnpunk.game.domain.model.LibUIConfig
import com.pixnpunk.game.domain.model.WindowType

internal class QspEngineBridgeImpl : QSPLib(), NativeEngineBridge {

    private var engineListener: NativeEngineListener? = null

    private var gameStartTime: Long = 0L
    private var lastMsCountCallTime: Long = 0L

    override fun initializeEngine() = init()

    override fun destroyEngine() = terminate()

    override fun setListener(
        listener: NativeEngineListener
    ) {
        this.engineListener = listener
    }

    override fun executeAction(index: Int): Boolean {
        executeCommand { setSelActIndex(index, false) }
        return executeCommand { execSelAction(true) }
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
                Action: ${errorData.actIndex}
                Line: ${errorData.intLineNum}
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
        file.readBytes().getOrNull().let { data ->
            executeCommand { loadGameWorldFromData(data, isNewGame) }
        }

    override suspend fun loadGameState(
        file: VirtualFile
    ): Boolean =
        file.readBytes().getOrNull().let { data ->
            executeCommand { openSavedGameFromData(data, true) }
        }

    override suspend fun saveGameState(
        file: VirtualFile
    ): Boolean =
        saveGameAsData(false).let { data ->
            file.writeBytes(data).isSuccess
        }

    override fun restartGame(): Boolean =
        executeCommand {
            gameStartTime = System.nanoTime()
            lastMsCountCallTime = 0L

            restartGame(true)
        }

    override fun selectObject(index: Int): Boolean =
        executeCommand { setSelObjIndex(index, true) }

    override fun onInputAreaClicked(text: String) {
        setInputStrText(text)
        executeCommand { execUserInput(true) }
    }

    private fun loadUIConfiguration(isNewDesc: Boolean) =
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

    override fun onRefreshInt(isForced: Boolean, isNewDesc: Boolean) {
        val listener = engineListener ?: return

        val mainDesc = mainDesc ?: ""
        val varsDesc = varsDesc ?: ""

        val actions = getActions()?.mapNotNull { it.toRawItem() }.orEmpty()
        val objects = getObjects()?.mapNotNull { it?.toObjectRawItem() }.orEmpty()

        listener.onStateRefreshed(
            mainDesc,
            varsDesc,
            loadUIConfiguration(isNewDesc),
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

    override fun onShowMessage(text: String?) {
        val safeMessage = text ?: return
        engineListener?.onShowMessage(safeMessage)
    }

    override fun onShowImage(file: String?) {
        if (!file.isNullOrBlank()) {
            engineListener?.onShowImage(file)
        }
    }

    override fun onInputBox(text: String?): String {
        val safePrompt = text ?: ""
        return engineListener?.onInputBox(safePrompt) ?: ""
    }

    override fun onShowMenu(items: Array<out ListItem>?): Int {
        if (items.isNullOrEmpty()) return -1
        val mappedItems = items.mapNotNull { it.toRawItem() }
        return engineListener?.onShowMenu(mappedItems) ?: -1
    }

    override fun onShowWindow(type: Int, toShow: Boolean) {
        val windowType = mapQspWindowType(type) ?: return
        engineListener?.onShowWindow(windowType, toShow)
    }

    override fun onOpenGame(file: String?, isNewGame: Boolean) {
        val safePath = file ?: return
        engineListener?.onOpenGame(safePath, isNewGame)
    }

    override fun onOpenGameStatus(file: String?) {
        val safePath = file ?: return
        engineListener?.onLoadSave(safePath)
    }

    override fun onSaveGameStatus(file: String?) {
        val safePath = file ?: return
        engineListener?.onSaveGame(safePath)
    }

    override fun onCloseFile(file: String?) {
        engineListener?.onCloseAudio(file)
    }

    override fun onIsPlayingFile(file: String?): Boolean {
        if (file.isNullOrBlank()) return false
        return engineListener?.onCheckAudioPlaying(file) ?: false
    }

    override fun onPlayFile(file: String?, volume: Int) {
        if (!file.isNullOrBlank()) {
            engineListener?.onPlayAudio(file, volume)
        }
    }

    private fun mapQspWindowType(type: Int): WindowType? = when (type) {
        Window.MAIN -> WindowType.MAIN
        Window.VARS -> WindowType.VARS
        Window.ACTS -> WindowType.ACTS
        Window.OBJS -> WindowType.OBJS
        Window.INPUT -> WindowType.INPUT
        Window.VIEW -> WindowType.VIEW
        else -> null
    }

    private fun ListItem?.toRawItem(): RawItem? {
        if (this == null) return null

        val safeName = name ?: return null
        val safeImage = image ?: ""

        return RawItem(safeName, safeImage)
    }

    private fun ObjectItem?.toObjectRawItem(): RawItem? {
        if (this == null) return null
        val safeName = name ?: return null
        val safeImage = image ?: ""

        return RawItem(safeName, safeImage)
    }
}
