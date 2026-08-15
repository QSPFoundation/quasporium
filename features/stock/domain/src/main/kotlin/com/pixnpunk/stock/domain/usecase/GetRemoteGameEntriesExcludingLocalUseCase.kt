package com.pixnpunk.stock.domain.usecase

import androidx.paging.filter
import com.pixnpunk.stock.domain.model.SearchState
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.stock.domain.repository.RemoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class GetRemoteGameEntriesExcludingLocalUseCase(
    private val remoteRepository: RemoteRepository,
    private val localRepository: LocalRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun execute(state: SearchState) =
        localRepository.observeEntryUrls().map { it.toSet() }.flatMapLatest { urls ->
            remoteRepository.getGameEntriesPager(state).map { pagingData ->
                pagingData.filter { gameEntry -> !urls.contains(gameEntry.fileUrl) }
            }
        }
}
