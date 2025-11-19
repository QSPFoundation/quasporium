package com.pixnpunk.database.model

import androidx.room.Entity
import androidx.room.Fts4
import com.pixnpunk.dto.GameListType

@Entity(tableName = "game_fts")
@Fts4(contentEntity = GameEntity::class)
data class GameFTS(
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
    val gameDirPath: String = "",
    val gameFilesPath: List<String> = emptyList()
)