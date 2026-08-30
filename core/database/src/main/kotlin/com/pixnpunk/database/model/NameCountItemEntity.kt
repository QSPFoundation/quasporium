package com.pixnpunk.database.model

sealed interface NameCountItemEntity {
    val name: String
    val gamesCount: Int
}
