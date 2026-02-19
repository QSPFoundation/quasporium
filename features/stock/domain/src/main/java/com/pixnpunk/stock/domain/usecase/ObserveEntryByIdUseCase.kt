package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.stock.domain.repository.LocalRepository

class ObserveEntryByIdUseCase(
    private val repository: LocalRepository
) {
    fun execute(entryId: Long) = repository.observeEntryById(entryId)
}