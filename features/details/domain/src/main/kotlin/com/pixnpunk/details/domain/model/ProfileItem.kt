package com.pixnpunk.details.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfileItem(
    val id: Long,
    val name: String,
    val timestamp: Long,
    val lastPlayedFile: String?,
    val accentColor: Long,
    val backImagePath: String
)
