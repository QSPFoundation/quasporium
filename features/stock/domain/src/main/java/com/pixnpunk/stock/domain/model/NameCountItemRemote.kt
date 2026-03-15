package com.pixnpunk.stock.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NameCountItemRemote(
    @SerialName("names") val name: String,
    @SerialName("games_count") val gamesCount: Int
)