package com.pixnpunk.details.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SaveItem(
    val id: Long,
    val profileId: Long = 0,
    val userTitle: String = "",
    val filePath: String = ""
)
