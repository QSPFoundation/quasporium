package com.pixnpunk.stock.domain.usecase

import androidx.paging.PagingData
import androidx.paging.filter
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.domain.repository.LocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class FetchRemoteGamesExcludingLocalUseCase(
    private val fetchRemoteSourceUseCase: FetchRemoteSourceUseCase,
    private val localRepository: LocalRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun execute(): Flow<PagingData<GameEntry>> =
        localRepository.observeEntryUrls().map { it.toSet() }.flatMapLatest { urls ->
            fetchRemoteSourceUseCase.execute().map { pagingData ->
                pagingData.filter { gameEntry -> !urls.contains(gameEntry.fileUrl) }
            }
        }
}