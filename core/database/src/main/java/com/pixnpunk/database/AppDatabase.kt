package com.pixnpunk.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pixnpunk.database.dao.AuthorDao
import com.pixnpunk.database.dao.GameDao
import com.pixnpunk.database.dao.SaveSlotDao
import com.pixnpunk.database.dao.TranslatorDao
import com.pixnpunk.database.model.AuthorEntity
import com.pixnpunk.database.model.GameEntity
import com.pixnpunk.database.model.GameFTS
import com.pixnpunk.database.model.SaveSlotEntity
import com.pixnpunk.database.model.TranslatorEntity

@Database(
    entities = [
        GameEntity::class,
        SaveSlotEntity::class,
        GameFTS::class,
        AuthorEntity::class,
        TranslatorEntity::class
    ],
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5)
    ],
    version = 6
)
@TypeConverters(ListStringConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun authorDao(): AuthorDao
    abstract fun translatorDao(): TranslatorDao
    abstract fun saveSlotDao(): SaveSlotDao
}