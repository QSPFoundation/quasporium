package com.pixnpunk.database.di

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pixnpunk.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import kotlin.time.Clock

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

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `game_fts`")
        db.execSQL(
            """
            CREATE VIRTUAL TABLE IF NOT EXISTS `game_fts` USING FTS4(
                `author`, `portedBy`, `title`, `descUrl`, 
                content=`game`, tokenize=unicode61
            )
        """.trimIndent()
        )
        db.execSQL("INSERT INTO `game_fts`(`game_fts`) VALUES ('rebuild')")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `profile` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `gameId` INTEGER NOT NULL,
                `profileName` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `totalPlayTime` INTEGER NOT NULL,
                `lastPlayedFile` TEXT,
                `accentColor` INTEGER NOT NULL DEFAULT 0,
                `backImagePath` TEXT NOT NULL DEFAULT '',
                FOREIGN KEY(`gameId`) REFERENCES `game`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_profile_gameId` ON `profile` (`gameId`)")

        val cursorGame = db.query("SELECT id, gameFilesPath FROM game")

        if (cursorGame.moveToFirst()) {
            do {
                val gameId = cursorGame.getLong(
                    cursorGame.getColumnIndexOrThrow("id")
                )
                val filesString = cursorGame.getString(
                    cursorGame.getColumnIndexOrThrow("gameFilesPath")
                )

                db.insert(
                    "profile",
                    SQLiteDatabase.CONFLICT_REPLACE,
                    ContentValues().apply {
                        put("gameId", gameId)
                        put("profileName", "Main")
                        put("timestamp", Clock.System.now().toEpochMilliseconds())
                        put("totalPlayTime", 0L)
                        put("lastPlayedFile", filesString?.split(", ")?.firstOrNull())
                    }
                )
            } while (cursorGame.moveToNext())
        }
        cursorGame.close()

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `game_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `listId` TEXT NOT NULL,
                `author` TEXT NOT NULL,
                `portedBy` TEXT NOT NULL,
                `version` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `lang` TEXT NOT NULL,
                `player` TEXT NOT NULL,
                `gameIconPath` TEXT NOT NULL,
                `fileUrl` TEXT NOT NULL,
                `fileSize` INTEGER NOT NULL,
                `fileExt` TEXT NOT NULL,
                `descUrl` TEXT NOT NULL,
                `pubDate` TEXT NOT NULL,
                `modDate` TEXT NOT NULL,
                `gameDirPath` TEXT NOT NULL
            )
        """
        )

        db.execSQL(
            """
            INSERT INTO `game_new` (
                id, listId, author, portedBy, version, title, lang, player, 
                gameIconPath, fileUrl, fileSize, fileExt, descUrl, pubDate, modDate, gameDirPath
            )
            SELECT 
                id, listId, author, portedBy, version, title, lang, player, 
                gameIconPath, fileUrl, fileSize, fileExt, descUrl, pubDate, modDate, gameDirPath
            FROM `game`
        """
        )

        db.execSQL("DROP TABLE `game`")
        db.execSQL("ALTER TABLE `game_new` RENAME TO `game`")
    }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppDatabase::class.java,
            name = "games.db"
        )
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_5_6,
                MIGRATION_6_7
            )
            .build()
    }
    single { get<AppDatabase>().gameDao() }
    single { get<AppDatabase>().authorDao() }
    single { get<AppDatabase>().translatorDao() }
    single { get<AppDatabase>().profileDao() }
    single { get<AppDatabase>().saveSlotDao() }
}