package com.pixnpunk.game.data.engine.jni

import com.pixnpunk.game.data.vfs.VirtualFile

internal interface NativeEngineBridge {
    fun initializeEngine()
    fun destroyEngine()

    fun executeAction(index: Int): Boolean
    fun executeCounter(): Boolean
    fun executeString(input: String): Boolean

    suspend fun loadGame(file: VirtualFile, isNewGame: Boolean = true): Boolean
    suspend fun saveGameState(file: VirtualFile): Boolean
    suspend fun loadGameState(file: VirtualFile): Boolean

    fun restartGame(): Boolean
    fun selectObject(index: Int): Boolean

    fun onInputAreaClicked(text: String)

    fun setListener(listener: NativeEngineListener)
}
