package com.pixnpunk.game.presentation

sealed interface GameEvent {
    data class ShowSnackbar(val message: String) : GameEvent
}
