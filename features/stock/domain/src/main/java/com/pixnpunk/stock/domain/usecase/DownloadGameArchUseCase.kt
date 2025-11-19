package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.stock.domain.repository.RemoteRepository

class DownloadGameArchUseCase(
    private val remoteRepository: RemoteRepository
) {
    suspend fun execute(downloadLink: String, destDirPath: String) =
        remoteRepository.downloadGameArch(downloadLink, destDirPath)
}