package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.stock.domain.repository.LocalRepository

class DeleteLocalGameEntriesUseCase(
    private val localRepository: LocalRepository
) {
    suspend fun execute(ids: Set<Long>): Int = localRepository.deleteGameEntries(ids)
}
