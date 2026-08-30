package com.pixnpunk.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pixnpunk.database.model.TranslatorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslatorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(translators: List<TranslatorEntity>)

    @Query("SELECT * FROM translators ORDER BY RANDOM() LIMIT 3")
    fun getRandomTranslators(): Flow<List<TranslatorEntity>>

    @Query("SELECT * FROM translators WHERE name LIKE '%' || :query || '%' ORDER BY name ASC LIMIT 3")
    fun searchTranslatorLocal(query: String): Flow<List<TranslatorEntity>>

    @Query("DELETE FROM translators")
    suspend fun clearAll()
}
