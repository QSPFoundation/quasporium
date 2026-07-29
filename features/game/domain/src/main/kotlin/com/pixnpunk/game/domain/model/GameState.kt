package com.pixnpunk.game.domain.model

sealed interface GameState {
    data object Idle : GameState
    data object Loading : GameState

    data class Running(
        val gameId: GameId,
        val uiConfig: LibUIConfig,
        val mainDescription: String,
        val varsDescription: String,
        val actions: List<InteractableItem>,
        val objects: List<InteractableItem>,
        val activeDialog: EngineDialog? = null,
        val activeWindows: Set<WindowType> = setOf(
            WindowType.MAIN, WindowType.INPUT, WindowType.ACTS
        )
    ) : GameState
}
