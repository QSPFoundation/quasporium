package com.pixnpunk.stock.domain.repository

import androidx.paging.PagingData
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.domain.model.SearchState
import kotlinx.coroutines.flow.Flow

interface LocalRepository {
    fun observeEntryUrls(): Flow<List<String>>
    fun observeEntryById(entryId: Long): Flow<GameEntry?>
    fun getGameEntriesPager(searchState: SearchState): Flow<PagingData<GameEntry>>
    suspend fun deleteGameEntries(idsDelete: Set<Long>): Int
    suspend fun getGameEntryById(entryId: Long): GameEntry
    suspend fun updateEntry(entry: GameEntry)
    suspend fun upsertEntry(entry: GameEntry)
    suspend fun insertEntry(newEntry: GameEntry)
    suspend fun fillAndInsertEntry(entry: GameEntry)
    suspend fun migrateIfNeeded()
}
