package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.stock.domain.repository.LocalRepository

class FetchLocalSourceUseCase(
    private val repository: LocalRepository
) {
    suspend fun execute() = repository.getAllPagedGameEntries()
}