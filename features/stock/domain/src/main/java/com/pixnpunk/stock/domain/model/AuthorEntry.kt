package com.pixnpunk.stock.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthorEntry(
    val name: String,
    val gamesCount: Int
)