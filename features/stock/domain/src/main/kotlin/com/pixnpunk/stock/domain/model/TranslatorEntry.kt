package com.pixnpunk.stock.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TranslatorEntry(
    val name: String,
    val gamesCount: Int
)
