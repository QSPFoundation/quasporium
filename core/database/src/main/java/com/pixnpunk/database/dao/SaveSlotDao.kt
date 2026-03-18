package com.pixnpunk.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pixnpunk.database.model.SaveSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaveSlotDao {
    @Query("SELECT * FROM save WHERE gameId = :gameId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSaves(gameId: Long, limit: Int): Flow<List<SaveSlotEntity>>

    @Query("SELECT * FROM save WHERE gameId = :gameId ORDER BY timestamp DESC")
    suspend fun getAllSaves(gameId: Long): List<SaveSlotEntity>

    @Query("SELECT count(*) FROM save WHERE gameId = :gameId ORDER BY timestamp DESC")
    suspend fun getSavesCount(gameId: Long): Int

    @Query("SELECT * FROM save WHERE gameId = :gameId AND userTitle = :title")
    suspend fun searchByTitle(gameId: Long, title: String): SaveSlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(saveSlot: SaveSlotEntity)

    @Update
    suspend fun update(saveSlot: SaveSlotEntity)

    @Delete
    suspend fun delete(saveSlot: SaveSlotEntity)
}