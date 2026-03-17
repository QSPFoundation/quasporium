package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.stock.domain.repository.RemoteRepository

class GetTranslatorSuggestionUseCase(
    private val remoteRepository: RemoteRepository
) {
    fun execute(query: String) = remoteRepository.getTranslatorSuggestions(query)
}