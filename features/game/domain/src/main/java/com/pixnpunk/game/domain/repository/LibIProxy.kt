package com.pixnpunk.game.domain.repository

import com.pixnpunk.game.domain.model.LibGameState
import com.pixnpunk.game.domain.model.LibReturnValue
import kotlinx.coroutines.CompletableDeferred

interface LibIProxy {
    fun startLibThread()
    fun stopLibThread()
    fun runGame(gameId: Long, gameTitle: String, gameDirPath: String, gameFilePath: String)
    fun restartGame()
    fun loadGameState(path: String)
    fun saveGameState(path: String)
    fun onActionClicked(index: Int)
    fun onObjectSelected(index: Int)
    fun onInputAreaClicked(code: String)
    fun execute(code: String)
    fun executeCounter()
    val gameState: LibGameState
    val gameInterface: GameInterface
    var returnValueDeferred: CompletableDeferred<LibReturnValue>
}
