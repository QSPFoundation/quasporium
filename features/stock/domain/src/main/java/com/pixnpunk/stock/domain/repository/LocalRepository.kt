package com.pixnpunk.stock.domain.repository

import androidx.paging.PagingData
import com.pixnpunk.dto.GameEntry
import kotlinx.coroutines.flow.Flow

interface LocalRepository {
    fun observeEntryUrls(): Flow<List<String>>
    fun observeEntryById(entryId: Long): Flow<GameEntry>
    suspend fun deleteGameEntries(idsDelete: Set<Long>): Int
    suspend fun getGameEntryById(entryId: Long): GameEntry
    suspend fun getAllPagedGameEntries(): Flow<PagingData<GameEntry>>
    suspend fun searchGameEntriesByQuery(query: String): Flow<PagingData<GameEntry>>
    suspend fun updateEntry(entry: GameEntry)
    suspend fun updateOrInsertEntry(entry: GameEntry)
    suspend fun insertDefaultEntry(newEntry: GameEntry)
    suspend fun fillAndInsertEntry(entry: GameEntry)
}