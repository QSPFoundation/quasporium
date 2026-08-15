package com.pixnpunk.dto

import kotlinx.serialization.Serializable

@Serializable
data class LaunchGameEntity(
    val profileId: Long,
    val entryFile: String,
    val saveFile: String? = null,
    val gameEntry: GameEntry
)
