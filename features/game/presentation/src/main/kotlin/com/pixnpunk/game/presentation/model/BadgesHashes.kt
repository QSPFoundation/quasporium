package com.pixnpunk.game.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class BadgesHashes(
    val mainHash: Int = 0,
    val actionsHash: Int = 0,
    val extraHash: Int = 0,
    val objectsHash: Int = 0
)
