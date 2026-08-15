package com.pixnpunk.details.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class GameSaveItem(
    val displayName: String,
    val path: String
)
