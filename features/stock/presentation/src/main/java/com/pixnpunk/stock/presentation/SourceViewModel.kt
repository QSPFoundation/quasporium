package com.pixnpunk.stock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.domain.usecase.FetchLocalSourceUseCase
import com.pixnpunk.stock.domain.usecase.FetchRemoteGamesExcludingLocalUseCase
import com.pixnpunk.stock.domain.usecase.SearchGameEntryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class SourceViewModel(
    private val fetchRemoteSourceUseCase: FetchRemoteGamesExcludingLocalUseCase,
    private val fetchLocalSourceUseCase: FetchLocalSourceUseCase,
    private val searchGameEntryUseCase: SearchGameEntryUseCase
) : ViewModel(), KoinComponent {
    private val _remotePagingFlow = MutableSharedFlow<PagingData<GameEntry>>(replay = 1)
    val remotePagingFlow: Flow<PagingData<GameEntry>> = _remotePagingFlow.asSharedFlow()

    private val _localPagingFlow = MutableSharedFlow<PagingData<GameEntry>>(replay = 1)
    val localPagingFlow: Flow<PagingData<GameEntry>> = _localPagingFlow.asSharedFlow()

    fun loadLocalData() {
        viewModelScope.launch {
            fetchLocalSourceUseCase.execute()
                .cachedIn(viewModelScope)
                .collect { _localPagingFlow.emit(it) }
        }
    }

    fun loadRemoteData() {
        viewModelScope.launch {
            fetchRemoteSourceUseCase.execute()
                .cachedIn(viewModelScope)
                .collect { _remotePagingFlow.emit(it) }
        }
    }

    fun searchGameEntry(query: String) {
        viewModelScope.launch {
            searchGameEntryUseCase.execute(query)
                .cachedIn(viewModelScope)
                .collect { _localPagingFlow.emit(it) }
        }
    }
}