package com.pixnpunk.stock.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NameCountItemRemote(
    @SerialName("names") val name: String,
    @SerialName("games_count") val gamesCount: Int
)
