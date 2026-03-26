package com.pixnpunk.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "authors")
data class AuthorEntity(
    @PrimaryKey override val name: String,
    override val gamesCount: Int
) : NameCountItemEntity