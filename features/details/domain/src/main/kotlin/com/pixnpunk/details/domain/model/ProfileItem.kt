package com.pixnpunk.details.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfileItem(
    val id: Long,
    val timestamp: Long,
    val name: String,
    val lastPlayedFile: String?
)
