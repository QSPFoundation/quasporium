package com.pixnpunk.stock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.domain.usecase.FetchLocalSourceUseCase
import com.pixnpunk.stock.domain.usecase.FetchRemoteGamesExcludingLocalUseCase
import com.pixnpunk.stock.domain.usecase.SearchLocalGameEntryUseCase
import com.pixnpunk.stock.domain.usecase.SearchRemoteGameEntryUseCase
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
    private val searchLocalGameEntryUseCase: SearchLocalGameEntryUseCase,
    private val searchRemoteGameEntryUseCase: SearchRemoteGameEntryUseCase
) : ViewModel(), KoinComponent {

    private val _remoteSearchQuery = MutableStateFlow("")
    private val _localSearchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val remotePagingFlow: Flow<PagingData<GameEntry>> =
        _remoteSearchQuery.flatMapLatest { query ->
            flow {
                emitAll(
                    if (query.isBlank()) {
                        fetchRemoteSourceUseCase.execute()
                    } else {
                        searchRemoteGameEntryUseCase.execute(query)
                    }
                )
            }
        }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val localPagingFlow: Flow<PagingData<GameEntry>> =
        _localSearchQuery.flatMapLatest { query ->
            flow {
                emitAll(
                    if (query.isBlank()) {
                        fetchLocalSourceUseCase.execute()
                    } else {
                        searchLocalGameEntryUseCase.execute(query)
                    }
                )
            }
        }.cachedIn(viewModelScope)

    fun localSearchGameEntry(query: String) {
        _localSearchQuery.value = query
    }

    fun remoteSearchGameEntry(query: String) {
        _remoteSearchQuery.value = query
    }
}