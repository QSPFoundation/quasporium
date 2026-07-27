package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.domain.repository.LocalRepository

class FillAndInsertEntryUseCase(
    private val repository: LocalRepository
) {
    suspend fun execute(newEntry: GameEntry) = repository.fillAndInsertEntry(newEntry)
}
