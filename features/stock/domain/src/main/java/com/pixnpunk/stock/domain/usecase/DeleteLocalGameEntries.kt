package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.domain.repository.LocalRepository

class DeleteLocalGameEntries(
    private val localRepository: LocalRepository
) {
    suspend fun execute(entries: Set<GameEntry>): Int = localRepository.deleteGameEntries(entries)
}