package com.pixnpunk.stock.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RemoteGameMetaLink(
    val url: String? = null,
    val label: String,
    val active: Boolean
)