package com.pixnpunk.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                synchronized(this) {
                    instance = Room.databaseBuilder<AppDatabase>(
                        context = context.applicationContext,
                        name = context.applicationContext.getDatabasePath("games.db").absolutePath
                    ).build()
                }
            }
            return instance!!
        }
    }
}