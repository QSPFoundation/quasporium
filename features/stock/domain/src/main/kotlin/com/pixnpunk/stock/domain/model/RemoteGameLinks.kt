package com.pixnpunk.stock.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("links")
data class RemoteGameLinks(
    val first: String,
    val last: String,
    val prev: String? = null,
    val next: String? = null
)
