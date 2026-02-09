package com.pixnpunk.stock.domain.usecase

import androidx.paging.PagingData
import androidx.paging.filter
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FetchRemoteGamesExcludingLocalUseCase(
    private val fetchRemoteSourceUseCase: FetchRemoteSourceUseCase,
    private val localRepository: LocalRepository
) {
    suspend fun execute(): Flow<PagingData<GameEntry>> =
        fetchRemoteSourceUseCase.execute().combine(
            localRepository.observeEntryUrls().map { list -> list.toSet() }
        ) { pagingData, urls ->
            pagingData.filter { gameEntry -> !urls.contains(gameEntry.fileUrl) }
        }
}