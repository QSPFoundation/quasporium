package com.pixnpunk.stock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.domain.usecase.FetchLocalSourceUseCase
import com.pixnpunk.stock.domain.usecase.FetchRemoteGamesExcludingLocalUseCase
import com.pixnpunk.stock.domain.usecase.SearchGameEntryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent

class SourceViewModel(
    private val fetchRemoteSourceUseCase: FetchRemoteGamesExcludingLocalUseCase,
    private val fetchLocalSourceUseCase: FetchLocalSourceUseCase,
    private val searchGameEntryUseCase: SearchGameEntryUseCase
) : ViewModel(), KoinComponent {
    private val _searchQuery = MutableStateFlow("")

    val remotePagingFlow: Flow<PagingData<GameEntry>> = flow {
        emitAll(fetchRemoteSourceUseCase.execute())
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val localPagingFlow: Flow<PagingData<GameEntry>> =
        _searchQuery.flatMapLatest { query ->
            flow {
                emitAll(
                    if (query.isBlank()) {
                        fetchLocalSourceUseCase.execute()
                    } else {
                        searchGameEntryUseCase.execute(query)
                    }
                )
            }
        }.cachedIn(viewModelScope)

    fun searchGameEntry(query: String) {
        _searchQuery.value = query
    }
}