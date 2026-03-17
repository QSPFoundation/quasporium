package com.pixnpunk.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pixnpunk.database.model.AuthorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(authors: List<AuthorEntity>)

    @Query("SELECT * FROM authors ORDER BY RANDOM() LIMIT 3")
    fun getRandomAuthors(): Flow<List<AuthorEntity>>

    @Query("SELECT * FROM authors WHERE name LIKE '%' || :query || '%' ORDER BY name ASC LIMIT 3")
    fun searchAuthorsLocal(query: String): Flow<List<AuthorEntity>>

    @Query("DELETE FROM authors")
    suspend fun clearAll()
}