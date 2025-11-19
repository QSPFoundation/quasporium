package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.stock.domain.repository.LocalRepository

class SearchGameEntryUseCase(
    private val repository: LocalRepository
) {
    suspend fun execute(query: String) = repository.searchGameEntriesByQuery(query)
}