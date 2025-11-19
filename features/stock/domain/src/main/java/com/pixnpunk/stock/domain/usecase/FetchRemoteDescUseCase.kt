package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.stock.domain.repository.RemoteRepository

class FetchRemoteDescUseCase(
    private val remoteRepository: RemoteRepository
) {
    suspend fun execute(descUrl: String) = remoteRepository.fetchDescription(descUrl)
}