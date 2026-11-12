package com.pixnpunk.game.domain.repository

import com.pixnpunk.game.domain.model.GameState
import com.pixnpunk.game.domain.model.ScriptState
import kotlinx.coroutines.flow.StateFlow

interface GameEngineInteractor {
    val state: StateFlow<GameState>
    val scriptState: StateFlow<ScriptState>
    fun submitCommand(command: EngineCommand)
    fun setTimerPaused(isPaused: Boolean)
    fun shutdown()
}
