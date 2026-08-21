package com.pixnpunk.dto

import kotlinx.serialization.Serializable

@Serializable
data class LaunchGameEntity(
    val entryFile: String,
    val saveFile: String? = null,
    val gameEntry: GameEntry
)
