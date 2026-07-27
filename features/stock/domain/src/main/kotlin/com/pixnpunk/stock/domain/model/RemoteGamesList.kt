package com.pixnpunk.stock.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RemoteGamesList(
    val data: List<RemoteGameData>,
    val links: RemoteGameLinks,
    val meta: RemoteGameMeta
)
