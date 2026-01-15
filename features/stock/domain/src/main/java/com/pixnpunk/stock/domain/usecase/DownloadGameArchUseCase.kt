package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.domain.repository.RemoteRepository

class DownloadGameArchUseCase(
    private val remoteRepository: RemoteRepository
) {
    suspend fun execute(downloadEntry: GameEntry) =
        remoteRepository.downloadGame(downloadEntry)
}