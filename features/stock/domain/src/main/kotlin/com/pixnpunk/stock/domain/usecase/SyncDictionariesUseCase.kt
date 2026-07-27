package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.stock.domain.repository.RemoteRepository

class SyncDictionariesUseCase(
    private val remoteRepository: RemoteRepository
) {
    suspend fun execute() = remoteRepository.syncDictionaries()
}
