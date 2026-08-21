package com.pixnpunk.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "preset",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(index = true)
    val gameId: Long = 0,
    val presetName: String = "",
    val timestamp: Long = 0,
    val totalPlayTime: Long = 0,
    val lastPlayedFile: String? = null,
    val accentColor: Long = 0,
    val backImagePath: String = ""
)
