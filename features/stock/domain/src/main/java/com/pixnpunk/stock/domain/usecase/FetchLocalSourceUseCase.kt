package com.pixnpunk.stock.domain.usecase

import androidx.paging.PagingData
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow

class FetchLocalSourceUseCase(
    private val repository: LocalRepository
) {
    suspend fun execute(): Flow<PagingData<GameEntry>> = repository.getAllPagedGameEntries()
}