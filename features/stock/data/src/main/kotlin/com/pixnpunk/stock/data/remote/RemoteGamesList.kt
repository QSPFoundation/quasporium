package com.pixnpunk.stock.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RemoteGamesList(
    val data: List<RemoteGameData>,
    val links: RemoteGameLinks,
    val meta: RemoteGameMeta
)
