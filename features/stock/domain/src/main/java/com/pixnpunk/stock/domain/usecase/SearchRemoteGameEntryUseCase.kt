package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.stock.domain.repository.RemoteRepository

class SearchRemoteGameEntryUseCase(
    private val repository: RemoteRepository
) {
    suspend fun execute(query: String) = repository.searchGameEntriesByQuery(query)
}