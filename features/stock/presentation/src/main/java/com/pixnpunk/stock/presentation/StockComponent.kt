package com.pixnpunk.stock.presentation

import androidx.paging.PagingData
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.presentation.mvi.StockStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface StockComponent {
    val childStack: Value<ChildStack<*, Child>>

    val model: StateFlow<StockStore.State>

    val showDialogAdd: () -> Unit
    val navigateToSettings: () -> Unit

    val gameLocalPageFlow: Flow<PagingData<GameEntry>>
    val gameRemotePageFlow: Flow<PagingData<GameEntry>>

    fun startDeleteMode()
    fun stopDeleteMode()
    fun deleteEntries(entries: Set<GameEntry>)
    fun searchEntries(query: String)

    fun navigateTo(config: RealStockComponent.ChildConfig)
    fun navigateToDetails(entry: GameEntry)

    sealed class Child {
        class InstalledChild() : Child()
        class LibraryChild() : Child()
    }
}