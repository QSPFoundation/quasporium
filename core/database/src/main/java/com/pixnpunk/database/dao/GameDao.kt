package com.pixnpunk.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pixnpunk.database.model.GameEntity

@Dao
interface GameDao {
    @Query("SELECT * FROM game")
    suspend fun getAll(): List<GameEntity>

    @Query("SELECT * FROM game ORDER BY id")
    fun getAllPaged(): PagingSource<Int, GameEntity>

    @Query("SELECT * FROM game_fts WHERE title LIKE :query || '%' ")
    fun search(query: String): PagingSource<Int, GameEntity>

    @Query("SELECT count(*) FROM game")
    suspend fun countGames(): Int

    @Query("SELECT * FROM game WHERE id LIKE :id LIMIT 1")
    suspend fun getById(id: Long): GameEntity?

    @Query("SELECT * FROM game WHERE title LIKE :name LIMIT 1")
    suspend fun getByName(name: String?): GameEntity

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