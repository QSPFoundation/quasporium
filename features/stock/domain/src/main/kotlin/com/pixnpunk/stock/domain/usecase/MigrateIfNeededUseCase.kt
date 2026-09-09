package com.pixnpunk.stock.domain.usecase

import com.pixnpunk.stock.domain.repository.LocalRepository

class MigrateIfNeededUseCase(
    private val localRepository: LocalRepository
) {
    suspend fun execute() = localRepository.migrateIfNeeded()
}
