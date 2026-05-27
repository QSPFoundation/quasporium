package com.pixnpunk.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pixnpunk.database.model.GameEntity
import com.pixnpunk.database.model.GameWithSaves
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game")
    suspend fun getAll(): List<GameEntity>

    @Query("SELECT * FROM game ORDER BY id ASC")
    fun getAllAsc(): PagingSource<Int, GameEntity>

    @Query("SELECT * FROM game ORDER BY id DESC")
    fun getAllDesc(): PagingSource<Int, GameEntity>

    @Query("""
        SELECT game.* FROM game 
        JOIN game_fts ON game.id = game_fts.rowid
        WHERE game_fts MATCH :ftsQuery
        ORDER BY game.id ASC
    """)
    fun searchAsc(ftsQuery: String): PagingSource<Int, GameEntity>

    @Query("""
        SELECT game.* FROM game 
        JOIN game_fts ON game.id = game_fts.rowid
        WHERE game_fts MATCH :ftsQuery
        ORDER BY game.id DESC
    """)
    fun searchDesc(ftsQuery: String): PagingSource<Int, GameEntity>

    @Query("SELECT count(*) FROM game")
    suspend fun countGames(): Int

    @Query("SELECT * FROM game WHERE id LIKE :id LIMIT 1")
    suspend fun getById(id: Long): GameEntity?

    @Query("SELECT * FROM game WHERE id IN (:ids)")
    suspend fun getByIds(ids: Set<Long>): List<GameEntity>

    @Query("SELECT * FROM game WHERE title LIKE :name LIMIT 1")
    suspend fun getByName(name: String?): GameEntity

    @Query("SELECT fileUrl FROM game")
    fun getByUrl(): Flow<List<String>>

    @Transaction
    @Query("SELECT * FROM game WHERE id = :gameId")
    fun getGameWithSaves(gameId: Long): Flow<GameWithSaves>

    @Query("SELECT * FROM game WHERE id LIKE :id")
    fun subscribeById(id: Long): Flow<GameEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: GameEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<GameEntity>)

    @Update
    suspend fun update(game: GameEntity)

    @Delete
    suspend fun delete(game: GameEntity)

    @Delete
    suspend fun deleteAll(entriesDelete: List<GameEntity>): Int
}