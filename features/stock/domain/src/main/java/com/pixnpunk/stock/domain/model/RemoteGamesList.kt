package com.pixnpunk.stock.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("game_list")
data class RemoteGamesList(
    val version: Double,
    val id: String,
    val title: String,
    val text: String = "",
    @SerialName("max_pages")
    val maxPages: Int,
    val gameList: List<RemoteGameData>
)