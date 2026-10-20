package com.pixnpunk.stock.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("meta")
data class RemoteGameMeta(
    @SerialName("current_page") val currentPage: Int,
    val from: Int,
    @SerialName("last_page") val lastPage: Int,
    val links: List<RemoteGameMetaLink>,
    val path: String,
    @SerialName("per_page") val perPage: Int,
    val to: Int,
    val total: Int
)
