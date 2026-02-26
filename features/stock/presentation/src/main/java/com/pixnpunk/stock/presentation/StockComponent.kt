package com.pixnpunk.stock.presentation

import androidx.paging.PagingData
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.stock.presentation.mvi.StockStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface StockComponent {
    val model: StateFlow<StockStore.State>
    val settingsState: StateFlow<SettingsEntity>

    val showDialogAdd: () -> Unit
    val navigateToSettings: () -> Unit
    val navigateToDetails: (GameEntry) -> Unit

    val gameLocalPageFlow: Flow<PagingData<GameEntry>>
    val gameRemotePageFlow: Flow<PagingData<GameEntry>>

    fun startDeleteMode()
    fun stopDeleteMode()
    fun deleteEntries(entries: Set<GameEntry>)
    fun searchLocalEntries(query: String)
    fun searchRemoteEntries(query: String)
}