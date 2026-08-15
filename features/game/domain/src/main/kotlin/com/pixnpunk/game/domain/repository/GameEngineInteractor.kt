package com.pixnpunk.game.domain.repository

import com.pixnpunk.game.domain.model.GameState
import kotlinx.coroutines.flow.StateFlow

interface GameEngineInteractor {
    val state: StateFlow<GameState>
    fun submitCommand(command: EngineCommand)
    fun shutdown()
}
