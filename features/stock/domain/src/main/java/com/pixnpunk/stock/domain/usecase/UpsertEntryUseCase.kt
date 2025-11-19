package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.domain.repository.LocalRepository

class UpsertEntryUseCase(
    private val repository: LocalRepository
) {
    suspend fun execute(entry: GameEntry) = repository.updateOrInsertEntry(entry)
}