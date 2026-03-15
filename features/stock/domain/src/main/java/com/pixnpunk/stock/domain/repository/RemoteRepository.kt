package com.pixnpunk.stock.domain.repository

import androidx.paging.PagingData
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.domain.model.AuthorEntry
import com.pixnpunk.stock.domain.model.SearchState
import com.pixnpunk.stock.domain.model.TranslatorEntry
import kotlinx.coroutines.flow.Flow

interface RemoteRepository {
    fun getGameEntriesPager(searchState: SearchState): Flow<PagingData<GameEntry>>

    fun getAuthorSuggestions(query: String): Flow<List<AuthorEntry>>

    fun getTranslatorSuggestions(query: String): Flow<List<TranslatorEntry>>

    suspend fun downloadGame(downloadEntry: GameEntry)

    suspend fun syncDictionaries(): Result<Unit>
}