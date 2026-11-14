package com.pixnpunk.game.presentation

import androidx.annotation.StringRes

sealed interface GameEvent {
    data class ShowSnackbar(@param:StringRes val messageRes: Int) : GameEvent
    data class OpenLink(val url: String) : GameEvent
}
