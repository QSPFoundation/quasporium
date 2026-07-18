package com.pixnpunk.game.domain.repository

interface AudioPlayer {
    var isSoundEnabled: Boolean

    fun isPlayingFile(path: String): Boolean
    fun playFile(path: String, volume: Int)
    fun closeFile(path: String)
    fun closeAllFiles()

    fun pauseAll()
    fun resumeAll()
}
