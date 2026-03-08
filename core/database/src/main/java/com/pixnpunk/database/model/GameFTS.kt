package com.pixnpunk.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "game_fts")
@Fts4(contentEntity = GameEntity::class)
data class GameFTS(
    @ColumnInfo("author")
    val author: String = "",
    @ColumnInfo("title")
    val title: String = "",
)