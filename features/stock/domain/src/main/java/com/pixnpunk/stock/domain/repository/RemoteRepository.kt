package com.pixnpunk.stock.domain.repository

import androidx.paging.PagingData
import com.pixnpunk.dto.GameEntry
import kotlinx.coroutines.flow.Flow

interface RemoteRepository {
    suspend fun fetchDescription(descUrl: String): String
    suspend fun fetchRemoteSource(): Flow<PagingData<GameEntry>>
    suspend fun downloadGame(downloadEntry: GameEntry)
}