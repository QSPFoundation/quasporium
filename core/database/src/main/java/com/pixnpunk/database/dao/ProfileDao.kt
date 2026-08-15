package com.pixnpunk.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pixnpunk.database.model.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE gameId = :gameId ORDER BY timestamp DESC")
    fun observeProfilesByGameId(gameId: Long): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profile WHERE gameId = :gameId ORDER BY timestamp DESC")
    suspend fun getProfilesByGameId(gameId: Long): List<ProfileEntity>

    @Query("UPDATE profile SET timestamp = :newTimestamp WHERE id = :profileId")
    suspend fun updateTimestamp(profileId: Long, newTimestamp: Long)

    @Query("UPDATE profile SET lastPlayedFile = :newLastPlayedFile WHERE id = :profileId")
    suspend fun updateLastPlayedFile(profileId: Long, newLastPlayedFile: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ProfileEntity): Long

    @Update
    suspend fun update(profile: ProfileEntity)

    @Delete
    suspend fun delete(profile: ProfileEntity)
}
