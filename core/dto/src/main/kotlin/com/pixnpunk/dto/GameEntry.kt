package com.pixnpunk.dto

import kotlinx.serialization.Serializable

@Serializable
data class GameEntry(
    val id: Long = 0,
    val listId: GameListType = GameListType.Unknown,
    val author: String = "",
    val portedBy: String = "",
    val version: String = "",
    val title: String = "",
    val lang: String = "",
    val player: String = "",
    val gameIconPath: String = "",
    val fileUrl: String = "",
    val fileSize: Long = 0,
    val fileExt: String = "",
    val descUrl: String = "",
    val pubDate: String = "",
    val modDate: String = "",
    val gameDirPath: String = ""
)
