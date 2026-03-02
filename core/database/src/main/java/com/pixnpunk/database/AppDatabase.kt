package com.pixnpunk.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pixnpunk.database.dao.GameDao
import com.pixnpunk.database.model.GameEntity
import com.pixnpunk.database.model.GameFTS

@Database(
    entities = [GameEntity::class, GameFTS::class],
    version = 1
)
@TypeConverters(ListStringConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}