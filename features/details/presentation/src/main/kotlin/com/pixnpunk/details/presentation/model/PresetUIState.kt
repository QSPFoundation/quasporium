package com.pixnpunk.details.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class PresetUIState(
    val id: Long,
    val name: String,
    val lastPlayedFile: GameFileItem?,
    val saves: List<GameSaveItem>,
    val accentColor: Long,
    val backImagePath: String
)
