package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.stock.domain.repository.RemoteRepository

class ObserveInstallStateUseCase(
    private val remoteRepository: RemoteRepository
) {
    fun execute(fileUrl: String) = remoteRepository.observeInstallState(fileUrl)
}
