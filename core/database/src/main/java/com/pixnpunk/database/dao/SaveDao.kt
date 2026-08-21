package com.pixnpunk.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pixnpunk.database.model.SaveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaveDao {
    @Query("SELECT * FROM save WHERE gameId = :gameId ORDER BY timestamp DESC")
    fun observeSaves(gameId: Long): Flow<List<SaveEntity>>

    @Query("SELECT * FROM save WHERE gameId = :gameId ORDER BY timestamp DESC")
    suspend fun getAllSaves(gameId: Long): List<SaveEntity>

    @Query("SELECT count(*) FROM save WHERE gameId = :gameId ORDER BY timestamp DESC")
    suspend fun getSavesCount(gameId: Long): Int

    @Query("SELECT * FROM save WHERE gameId = :gameId AND userTitle = :title")
    suspend fun searchByTitle(gameId: Long, title: String): SaveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(saveSlot: SaveEntity)

    @Update
    suspend fun update(saveSlot: SaveEntity)

    @Delete
    suspend fun delete(saveSlot: SaveEntity)
}
