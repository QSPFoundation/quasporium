package com.pixnpunk.game.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class BadgesState(
    val hasNewMain: Boolean = false,
    val hasNewActions: Boolean = false,
    val hasNewExtra: Boolean = false,
    val hasNewObjects: Boolean = false
)