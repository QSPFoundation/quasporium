package com.libsdhqs.jni

abstract class QSLibSDH {
    @JvmRecord
    data class ListItem(val image: String?, val text: String?)

    @JvmRecord
    data class ExecutionState(val loc: String?, val actIndex: Int, val lineNum: Int)

    @JvmRecord
    data class VarValResp(val isSuccess: Boolean, val stringValue: String?, val intValue: Int)

    @JvmRecord
    data class ErrorData(val locName: String?, val errorNum: Int, val index: Int, val line: Int)

    companion object {
        init {
            System.loadLibrary("qslibsdh")
        }
    }

    // --- Lifecycle and State ---
    external fun init()
    external fun terminate()
    external fun enableDebugMode(isDebug: Boolean)
    external fun getCurStateData(): ExecutionState?
    external fun getFullRefreshCount(): Int
    external fun getQstFullPath(): String?
    external fun getCurLoc(): String?
    external fun getExprValue(): Any?

    // --- Version and compile date
    external fun getVersion(): String?
    external fun getCompiledDateTime(): String?

    // --- Callback's ---
//    external fun isInCallBack(): Boolean
//    external fun setCallBack(type: Int, func: QSP_CALLBACK)

    // --- Main Description ---
    external fun getMainDesc(): String?
    external fun isMainDescChanged(): Boolean

    // --- Variables Description ---
    external fun getVarsDesc(): String?
    external fun isVarsDescChanged(): Boolean
    external fun getVarValuesCount(name: String): Int
    external fun getVarValues(name: String, ind: Int): VarValResp?
    external fun getMaxVarsCount(): Int
    external fun getVarNameByIndex(index: Int): Any?

    // --- Input ---
    external fun setInputStrText(text: String)

    // --- Actions ---
    external fun getActions(): Array<ListItem?>?
    external fun isActionsChanged(): Boolean
    external fun executeSelActionCode(isRefresh: Boolean): Boolean
    external fun getSelActionIndex(): Int
    external fun setSelActionIndex(ind: Int, isRefresh: Boolean): Boolean

    // --- Objects ---
    external fun getObjects(): Array<ListItem?>?
    external fun isObjectsChanged(): Boolean
    external fun getSelObjectIndex(): Int
    external fun setSelObjectIndex(ind: Int, isRefresh: Boolean): Boolean

    // --- Code execution ---
    external fun execString(s: String, isRefresh: Boolean): Boolean
    external fun execLocationCode(name: String, isRefresh: Boolean): Boolean
    external fun execCounter(isRefresh: Boolean): Boolean
    external fun execUserInput(isRefresh: Boolean): Boolean

    // --- Errors ---
    external fun getLastErrorData(): ErrorData?
    external fun getErrorDesc(errorNum: Int): String?

    // --- Game ---
    external fun loadGameWorldFromData(data: ByteArray, fileName: String): Boolean
    external fun loadGameWorldFromFD(fileDescriptor: Int, fileName: String): Boolean
    external fun saveGameAsData(isRefresh: Boolean): ByteArray?
    external fun saveGameByFD(fileDescriptor: Int, isRefresh: Boolean): Boolean
    external fun openSavedGameFromData(data: ByteArray, isRefresh: Boolean): Boolean
    external fun openSavedGameFromFD(fileDescriptor: Int, isRefresh: Boolean): Boolean
    external fun restartGame(isRefresh: Boolean): Boolean

    // --- Menu ---
    external fun selectMenuItem(index: Int)

    // --- Callbacks from Native Code ---
    open fun onCallDebug(str: String?) {}
    open fun onRefreshInt() {}

    open fun onShowImage(path: String?) {}
    open fun onShowMessage(message: String?) {}
    open fun onShowWindow(type: Int, isShow: Boolean) {}

    open fun onPlayFile(path: String?, volume: Int) {}
    open fun onIsPlayingFile(path: String?): Boolean = false
    open fun onCloseFile(path: String?) {}

    open fun onOpenGame(path: String?) {}
    open fun onOpenGameStatus(filename: String?) {}
    open fun onSaveGameStatus(filename: String?) {}

    open fun onInputBox(prompt: String?): String = ""

    open fun onSetTimer(msecs: Int) {}
    open fun onGetMsCount(): Int = 0
    open fun onSleep(msecs: Int) {}

    open fun onAddMenuItem(name: String?, imgPath: String?) {}
    open fun onShowMenuOld() {}
    open fun onShowMenuNew(): Int = -1
    open fun onDeleteMenu() {}

    open fun onGetFileDesc(path: String?): Int = -1
}