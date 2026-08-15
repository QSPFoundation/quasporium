package com.pixnpunk.game.domain.model

sealed interface GameState {
    val activeDialog: EngineDialog?

    data class Idle(
        override val activeDialog: EngineDialog? = null
    ) : GameState

    data class Loading(
        override val activeDialog: EngineDialog? = null
    ) : GameState

    data class Running(
        val gameId: GameId,
        val uiConfig: LibUIConfig,
        val mainDescription: String,
        val varsDescription: String,
        val actions: List<InteractableItem>,
        val objects: List<InteractableItem>,
        override val activeDialog: EngineDialog? = null,
        val activeWindows: Set<WindowType> = setOf(
            WindowType.MAIN, WindowType.INPUT, WindowType.ACTS
        )
    ) : GameState
}
