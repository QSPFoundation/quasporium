package com.pixnpunk.game.domain.repository

import com.pixnpunk.game.domain.model.LibGameState
import com.pixnpunk.game.domain.model.LibReturnValue
import java.util.concurrent.CompletableFuture

interface LibIProxy {
    /**
     * Starts the library thread.
     */
    fun startLibThread()

    /**
     * Stops the library thread.
     */
    fun stopLibThread()
    fun enableDebugMode(isDebug: Boolean)
    fun runGame(gameId: Long, gameTitle: String, gameDirPath: String, gameFilePath: String)
    fun restartGame()
    fun loadGameState(path: String)
    fun saveGameState(path: String)
    fun onActionClicked(index: Int)
    fun onObjectSelected(index: Int)
    fun onInputAreaClicked(code: String)

    /**
     * Starts execution of the specified line of code in the library.
     */
    fun execute(code: String)

    /**
     * Starts processing the location counter in the library.
     */
    fun executeCounter()
    val gameState: LibGameState
    val gameInterface: GameInterface
    var returnValueFuture: CompletableFuture<LibReturnValue>
}