package com.pixnpunk.game.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RawItem(
    val text: String,
    val imagePath: String
)
