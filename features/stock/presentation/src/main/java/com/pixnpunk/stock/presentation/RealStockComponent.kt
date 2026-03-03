package com.pixnpunk.stock.presentation

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.settings.SettingsRepository
import com.pixnpunk.stock.presentation.mvi.RealStockStore
import com.pixnpunk.stock.presentation.mvi.StockStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealStockComponent(
    private val componentContext: ComponentContext,
    override val showDialogAdd: () -> Unit,
    override val navigateToSettings: () -> Unit,
    override val navigateToDetails: (GameEntry) -> Unit,
) : ComponentContext by componentContext, StockComponent, KoinComponent {

    private val sourceViewModel: SourceViewModel = get()

    private val store = instanceKeeper.getStore {
        RealStockStore(DefaultStoreFactory()).create()
    }

    override val settingsState: StateFlow<SettingsEntity> =
        get<SettingsRepository>().settingsFlow.stateIn(
            scope = CoroutineScope(Dispatchers.Main),
            started = SharingStarted.Eagerly,
            initialValue = get<SettingsRepository>().currentSettings
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<StockStore.State> = store.stateFlow
    override val gameRemotePageFlow: Flow<PagingData<GameEntry>> = sourceViewModel.remotePagingFlow
    override val gameLocalPageFlow: Flow<PagingData<GameEntry>> = sourceViewModel.localPagingFlow

    override fun toggleGameSelection(gameId: Long) =
        store.accept(StockStore.Intent.ToggleGameSelection(gameId))

    override fun addAllGames(gameIds: Set<Long>) =
        store.accept(StockStore.Intent.AddAllVisibleGames(gameIds))

    override fun enterDeleteMode() =
        store.accept(StockStore.Intent.EnterDeleteMode)

    override fun exitDeleteMode() =
        store.accept(StockStore.Intent.ExitDeleteMode)

    override fun deleteEntries() =
        store.accept(StockStore.Intent.DeleteSelectedGames)

    override fun searchLocalEntries(query: String) {
        sourceViewModel.localSearchGameEntry(query)
    }

    override fun searchRemoteEntries(query: String) {
        sourceViewModel.remoteSearchGameEntry(query)
    }
}