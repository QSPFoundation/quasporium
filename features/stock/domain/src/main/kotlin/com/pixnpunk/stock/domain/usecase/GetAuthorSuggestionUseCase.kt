package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.stock.domain.repository.RemoteRepository

class GetAuthorSuggestionUseCase(
    private val remoteRepository: RemoteRepository
) {
    fun execute(query: String) = remoteRepository.getAuthorSuggestions(query)
}
