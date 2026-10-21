package com.pixnpunk.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pixnpunk.database.di.ALL_MIGRATIONS
import com.pixnpunk.database.di.MIGRATION_1_2
import com.pixnpunk.database.di.MIGRATION_5_6
import com.pixnpunk.database.di.MIGRATION_6_7
import com.pixnpunk.database.model.GameEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Before
    @After
    fun deleteDatabase() {
        ApplicationProvider.getApplicationContext<android.content.Context>()
            .deleteDatabase(TEST_DB)
    }

    @Test
    fun migrate1To2RebuildsSearchIndex() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.insertGame(id = 1, title = "Dungeon", author = "Alice")
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        assertEquals(listOf(1L), db.search("Alice"))
    }

    @Test
    fun autoMigrations2To5KeepGames() {
        helper.createDatabase(TEST_DB, 2).use { db ->
            db.insertGame(id = 1, title = "Dungeon")
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true)

        db.query("SELECT title FROM game WHERE id = 1").use {
            it.moveToFirst()
            assertEquals("Dungeon", it.getString(0))
        }
    }

    @Test
    fun migrate5To6IndexesCyrillicCaseInsensitively() {
        helper.createDatabase(TEST_DB, 5).use { db ->
            db.insertGame(id = 1, title = "Подземелье", portedBy = "Переводчик")
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)

        assertEquals(listOf(1L), db.search("подземелье"))
        assertEquals(listOf(1L), db.search("ПЕРЕВОДЧИК"))
    }

    @Test
    fun migrate6To7MovesFirstEntryFileToMainPresetAndKeepsSaves() {
        helper.createDatabase(TEST_DB, 6).use { db ->
            db.insertGame(id = 1, gameFilesPath = "main.qsp, extra.qsp")
            db.insertGame(id = 2, gameFilesPath = "")
            db.insertSave(gameId = 1, title = "Slot")
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)

        db.query("SELECT gameId, presetName, lastPlayedFile FROM preset ORDER BY gameId").use {
            assertEquals(2, it.count)
            it.moveToFirst()
            assertEquals(1L, it.getLong(0))
            assertEquals("Main", it.getString(1))
            assertEquals("main.qsp", it.getString(2))
            it.moveToNext()
            assertEquals(2L, it.getLong(0))
            assertEquals("", it.getString(2))
        }
        db.query("SELECT userTitle FROM save WHERE gameId = 1").use {
            assertEquals(1, it.count)
        }
        db.query("PRAGMA table_info(`game`)").use {
            val columns = buildList { while (it.moveToNext()) add(it.getString(1)) }
            assertFalse("gameFilesPath" in columns)
        }
    }

    @Test
    fun releasedVersionsOpenWithRoomAndKeepSearchWorking() = runTest {
        for (version in RELEASED_VERSIONS) {
            deleteDatabase()
            helper.createDatabase(TEST_DB, version).use { db ->
                db.insertGame(id = 1, title = "Dungeon", gameFilesPath = "main.qsp")
            }

            val database = Room.databaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java,
                TEST_DB
            ).addMigrations(*ALL_MIGRATIONS).build()
            try {
                val dao = database.gameDao()
                assertNotNull("v$version", dao.getById(1))
                dao.insert(GameEntity(id = 2, title = "Castle", author = "Bob"))

                val sqlite = database.openHelper.writableDatabase
                assertEquals("v$version", listOf(2L), sqlite.search("Castle"))
                assertEquals("v$version", listOf(1L), sqlite.search("Dungeon"))
            } finally {
                database.close()
            }
        }
    }

    private fun SupportSQLiteDatabase.insertGame(
        id: Long,
        title: String = "",
        author: String = "",
        portedBy: String = "",
        gameFilesPath: String = ""
    ) {
        val values = ContentValues().apply {
            put("id", id)
            put("listId", "Unknown")
            put("author", author)
            put("portedBy", portedBy)
            put("title", title)
            put("fileSize", 0L)
            put("gameFilesPath", gameFilesPath)
            TEXT_COLUMNS.forEach { put(it, "") }
        }
        insert("game", SQLiteDatabase.CONFLICT_ABORT, values)
    }

    private fun SupportSQLiteDatabase.insertSave(gameId: Long, title: String) {
        val values = ContentValues().apply {
            put("gameId", gameId)
            put("userTitle", title)
            put("filePath", "")
            put("timestamp", 0L)
            put("playTime", 0L)
        }
        insert("save", SQLiteDatabase.CONFLICT_ABORT, values)
    }

    private fun SupportSQLiteDatabase.search(query: String): List<Long> =
        query(
            "SELECT game.id FROM game JOIN game_fts ON game.id = game_fts.rowid " +
                "WHERE game_fts MATCH ? ORDER BY game.id",
            arrayOf(query)
        ).use { buildList { while (it.moveToNext()) add(it.getLong(0)) } }

    private companion object {
        const val TEST_DB = "migration-test.db"

        val RELEASED_VERSIONS = listOf(1, 2, 6)

        val TEXT_COLUMNS = listOf(
            "version", "lang", "player", "gameIconPath", "fileUrl",
            "fileExt", "descUrl", "pubDate", "modDate", "gameDirPath"
        )
    }
}
