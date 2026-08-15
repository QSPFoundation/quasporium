package com.pixnpunk.details.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ProfileUIState(
    val id: Long,
    val name: String,
    val lastPlayedFile: GameFileItem?,
    val saves: List<GameSaveItem>
)
