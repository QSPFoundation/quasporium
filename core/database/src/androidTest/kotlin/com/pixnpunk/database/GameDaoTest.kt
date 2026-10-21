package com.pixnpunk.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pixnpunk.database.dao.GameDao
import com.pixnpunk.database.model.GameEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: GameDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = database.gameDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun missingRowsReadAsNull() = runTest {
        dao.insert(GameEntity(id = 1, title = "Dungeon"))

        assertNull(dao.getById(2))
        assertNull(dao.getByName("Castle"))
        assertNull(dao.getByName(null))
    }

    @Test
    fun getByNameMatchesTitle() = runTest {
        dao.insert(GameEntity(id = 1, title = "Dungeon"))

        assertEquals(1L, dao.getByName("Dungeon")?.id)
    }

    @Test
    fun subscribeByIdFollowsInsertAndDelete() = runTest {
        val game = GameEntity(id = 1, title = "Dungeon")

        assertNull(dao.subscribeById(1).first())
        dao.insert(game)
        assertEquals(game, dao.subscribeById(1).first())
        dao.delete(game)
        assertNull(dao.subscribeById(1).first())
    }

    @Test
    fun deleteAllReturnsNumberOfDeletedRows() = runTest {
        val games = listOf(GameEntity(id = 1), GameEntity(id = 2))
        dao.insertAll(games)

        assertEquals(2, dao.deleteAll(games + GameEntity(id = 3)))
        assertEquals(0, dao.countGames())
    }
}
