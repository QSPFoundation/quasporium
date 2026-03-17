package com.pixnpunk.stock.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchToken(
    val value: String,
    val type: SearchTokenType,
    val displayText: String
)
