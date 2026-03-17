package com.pixnpunk.database.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pixnpunk.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `game_fts`")
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS `game_fts` USING FTS4(
                `author`, `portedBy`, `title`, `descUrl`, 
                content=`game`, tokenize=unicode61
            )
        """.trimIndent())
        db.execSQL("INSERT INTO `game_fts`(`game_fts`) VALUES ('rebuild')")
    }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppDatabase::class.java,
            name = "games.db"
        )
        .addMigrations(MIGRATION_5_6)
        .build()
    }
    single { get<AppDatabase>().gameDao() }
    single { get<AppDatabase>().authorDao() }
    single { get<AppDatabase>().translatorDao() }
    single { get<AppDatabase>().saveSlotDao() }
}