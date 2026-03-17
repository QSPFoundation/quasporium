package com.pixnpunk.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class GameWithSaves(
    @Embedded
    val game: GameEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "gameId"
    )
    val saves: List<SaveSlotEntity>
)