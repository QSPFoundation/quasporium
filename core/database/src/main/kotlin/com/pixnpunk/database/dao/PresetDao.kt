package com.pixnpunk.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pixnpunk.database.model.PresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM preset WHERE gameId = :gameId ORDER BY timestamp DESC")
    fun observePresets(gameId: Long): Flow<List<PresetEntity>>

    @Query("SELECT * FROM preset WHERE gameId = :gameId")
    suspend fun getAllPresets(gameId: Long): List<PresetEntity>

    @Query("SELECT backImagePath FROM preset WHERE id = :presetId")
    suspend fun getBackgroundPath(presetId: Long): String?

    @Query("UPDATE preset SET timestamp = :newTimestamp WHERE id = :presetId")
    suspend fun updateTimestamp(presetId: Long, newTimestamp: Long)

    @Query("UPDATE preset SET lastPlayedFile = :newLastPlayedFile WHERE id = :presetId")
    suspend fun updateLastPlayedFile(presetId: Long, newLastPlayedFile: String)

    @Query("UPDATE preset SET presetName = :name, accentColor = :accentColor WHERE id = :presetId")
    suspend fun updateVisuals(presetId: Long, name: String, accentColor: Long)

    @Query("UPDATE preset SET backImagePath = :path WHERE id = :presetId")
    suspend fun updateBackground(presetId: Long, path: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: PresetEntity): Long

    @Update
    suspend fun update(profile: PresetEntity)

    @Delete
    suspend fun delete(profile: PresetEntity)
}
