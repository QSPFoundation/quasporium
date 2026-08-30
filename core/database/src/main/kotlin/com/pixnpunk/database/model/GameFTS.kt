package com.pixnpunk.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

@Entity(tableName = "game_fts")
@Fts4(
    contentEntity = GameEntity::class,
    tokenizer = FtsOptions.TOKENIZER_UNICODE61
)
data class GameFTS(
    @ColumnInfo("author", defaultValue = "")
    val author: String = "",
    @ColumnInfo("portedBy", defaultValue = "")
    val portedBy: String = "",
    @ColumnInfo("title", defaultValue = "")
    val title: String = "",
    @ColumnInfo("descUrl", defaultValue = "")
    val descUrl: String = ""
)
