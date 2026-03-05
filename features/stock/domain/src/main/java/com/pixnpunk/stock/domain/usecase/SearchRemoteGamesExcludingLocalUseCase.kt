package com.pixnpunk.stock.domain.usecase

import androidx.paging.filter
import com.pixnpunk.stock.domain.repository.LocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class SearchRemoteGamesExcludingLocalUseCase(
    private val searchRemoteGameEntryUseCase: SearchRemoteGameEntryUseCase,
    private val localRepository: LocalRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun execute(query: String) =
        localRepository.observeEntryUrls().map { it.toSet() }.flatMapLatest { urls ->
            searchRemoteGameEntryUseCase.execute(query).map { pagingData ->
                pagingData.filter { gameEntry -> !urls.contains(gameEntry.fileUrl) }
            }
        }
}