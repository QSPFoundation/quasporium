package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.stock.domain.repository.RemoteRepository

class CancelInstallUseCase(
    private val remoteRepository: RemoteRepository
) {
    suspend fun execute(fileUrl: String) = remoteRepository.cancelInstall(fileUrl)
}
