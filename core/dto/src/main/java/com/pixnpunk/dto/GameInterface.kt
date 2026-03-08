package com.pixnpunk.dto

interface GameInterface {
    class UserCancelledException : Exception("User cancelled file/menu selection")

    fun requestReceiveFile(filePath: String)
    fun requestReceiveFile()
    fun requestCreateFile(path: String)
    fun requestCreateFile()
    fun isPlayingFile(filePath: String): Boolean
    fun closeAllFiles(): Result<Unit>
    fun closeFile(filePath: String)
    fun playFile(path: String, volume: Int)
    fun changeGameDir(filePath: String)
    fun showLibDialog(
        dialogType: LibTypeDialog,
        inputString: String = "",
        menuItems: List<LibGenItem> = emptyList(),
    )

    fun changeVisWindow(type: LibTypeWindow, show: Boolean)

    /**
     * Set the counter location processing interval to `delayMillis` milliseconds.
     */
    fun setCountInter(delayMillis: Long)

    /**
     * Execute `runnable` without processing the location counter.
     */
    fun doWithCounterDisabled(runnable: Runnable)

    fun setGameState(gameState: LibGameState)
    fun setUIConfig(uiConfig: LibUIConfig)
}
