package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.stock.domain.model.SearchState
import com.pixnpunk.stock.domain.repository.LocalRepository

class GetLocalGameEntriesUseCase(
    private val repository: LocalRepository
) {
    fun execute(state: SearchState) = repository.getGameEntriesPager(state)
}
