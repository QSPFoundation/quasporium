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
    @Query("SELECT * FROM save WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun observeSavesByProfileId(profileId: Long): Flow<List<SaveSlotEntity>>

    @Query("SELECT * FROM save WHERE profileId IN (SELECT id FROM profile WHERE gameId = :gameId)")
    fun observeAllSavesForGame(gameId: Long): Flow<List<SaveSlotEntity>>

    @Query("SELECT * FROM save WHERE profileId = :profileId")
    suspend fun getAllSaves(profileId: Long): List<SaveSlotEntity>

    @Query("SELECT count(*) FROM save WHERE profileId = :profileId ORDER BY timestamp DESC")
    suspend fun getSavesCount(profileId: Long): Int

    @Query("SELECT * FROM save WHERE profileId = :profileId AND userTitle = :title")
    suspend fun searchByTitle(profileId: Long, title: String): SaveSlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(saveSlot: SaveSlotEntity)

    @Update
    suspend fun update(saveSlot: SaveSlotEntity)

    @Delete
    suspend fun delete(saveSlot: SaveSlotEntity)
}
