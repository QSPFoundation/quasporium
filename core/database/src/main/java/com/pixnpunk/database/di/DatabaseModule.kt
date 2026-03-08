package com.pixnpunk.database.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pixnpunk.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `game_fts`")

        db.execSQL("DROP TRIGGER IF EXISTS room_fts_content_sync_game_fts_BEFORE_UPDATE")
        db.execSQL("DROP TRIGGER IF EXISTS room_fts_content_sync_game_fts_BEFORE_DELETE")
        db.execSQL("DROP TRIGGER IF EXISTS room_fts_content_sync_game_fts_AFTER_UPDATE")
        db.execSQL("DROP TRIGGER IF EXISTS room_fts_content_sync_game_fts_AFTER_INSERT")

        db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `game_fts` USING FTS4(`author`,`title`, content=`game`)")

        db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_game_fts_BEFORE_UPDATE BEFORE UPDATE ON `game` BEGIN DELETE FROM `game_fts` WHERE `docid`=OLD.`rowid`; END")
        db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_game_fts_BEFORE_DELETE BEFORE DELETE ON `game` BEGIN DELETE FROM `game_fts` WHERE `docid`=OLD.`rowid`; END")
        db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_game_fts_AFTER_UPDATE AFTER UPDATE ON `game` BEGIN INSERT INTO `game_fts`(`docid`, `author`, `title`) VALUES (NEW.`rowid`, NEW.`author`, NEW.`title`); END")
        db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_game_fts_AFTER_INSERT AFTER INSERT ON `game` BEGIN INSERT INTO `game_fts`(`docid`, `author`, `title`) VALUES (NEW.`rowid`, NEW.`author`, NEW.`title`); END")

        db.execSQL("INSERT INTO game_fts(game_fts) VALUES ('rebuild')")
    }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppDatabase::class.java,
            name = "games.db"
        )
        .addMigrations(MIGRATION_1_2)
        .build()
    }
    single { get<AppDatabase>().gameDao() }
}