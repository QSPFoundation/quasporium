package com.pixnpunk.stock.presentation

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.settings.SettingsRepository
import com.pixnpunk.stock.domain.model.SearchState
import com.pixnpunk.stock.domain.model.SearchToken
import com.pixnpunk.stock.domain.model.SortBy
import com.pixnpunk.stock.domain.usecase.GetAuthorSuggestionUseCase
import com.pixnpunk.stock.domain.usecase.GetLocalGameEntriesUseCase
import com.pixnpunk.stock.domain.usecase.GetRemoteGameEntriesExcludingLocalUseCase
import com.pixnpunk.stock.domain.usecase.GetTranslatorSuggestionUseCase
import com.pixnpunk.stock.presentation.mvi.RealStockStore
import com.pixnpunk.stock.presentation.mvi.StockStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealStockComponent(
    private val componentContext: ComponentContext,
    override val showDialogAdd: () -> Unit,
    override val navigateToSettings: () -> Unit,
    override val navigateToDetails: (GameEntry) -> Unit,
) : ComponentContext by componentContext, StockComponent, KoinComponent {

    private val componentScope = coroutineScope()

    private val settingsRepository: SettingsRepository = get()
    private val getLocalGameEntriesUseCase: GetLocalGameEntriesUseCase = get()
    private val getRemoteGameEntriesUseCase: GetRemoteGameEntriesExcludingLocalUseCase = get()
    private val getAuthorSuggestionUseCase: GetAuthorSuggestionUseCase = get()
    private val getTranslatorSuggestionUseCase: GetTranslatorSuggestionUseCase = get()

    private val store = instanceKeeper.getStore {
        RealStockStore(DefaultStoreFactory()).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<StockStore.State> = store.stateFlow

    override val settingsState: StateFlow<SettingsEntity> =
        settingsRepository.settingsFlow.stateIn(
            scope = componentScope,
            started = SharingStarted.Eagerly,
            initialValue = settingsRepository.currentSettings
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    override val gameRemotePageFlow: Flow<PagingData<GameEntry>> =
        store.stateFlow.flatMapLatest { state ->
            getRemoteGameEntriesUseCase.execute(
                SearchState(
                    state.searchQuery,
                    state.sortBy,
                    state.sortOrder,
                    state.selectedLang,
                    state.activeTokens
                )
            )
        }.cachedIn(componentScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val gameLocalPageFlow: Flow<PagingData<GameEntry>> =
        store.stateFlow.flatMapLatest { state ->
            getLocalGameEntriesUseCase.execute(
                SearchState(
                    state.searchQuery,
                    state.sortBy,
                    state.sortOrder,
                    state.selectedLang,
                    state.activeTokens
                )
            )
        }.cachedIn(componentScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val authorSuggestionsFlow: Flow<List<String>> =
        store.stateFlow.map { it.searchQuery }.distinctUntilChanged().flatMapLatest { query ->
            getAuthorSuggestionUseCase.execute(query).map { list -> list.map { it.name } }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val translatorSuggestionsFlow: Flow<List<String>> =
        store.stateFlow.map { it.searchQuery }.distinctUntilChanged().flatMapLatest { query ->
            getTranslatorSuggestionUseCase.execute(query).map { list -> list.map { it.name } }
        }

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

    override fun updateSearchQuery(query: String) =
        store.accept(StockStore.Intent.UpdateQuery(query))

    override fun changeSortOrder() =
        store.accept(StockStore.Intent.ToggleSortOrder)

    override fun changeSortBy(sortBy: SortBy) =
        store.accept(StockStore.Intent.SelectSort(sortBy))

    override fun changeSortLang(lang: String?) =
        store.accept(StockStore.Intent.SelectLanguage(lang))

    override fun toggleSearch(isExpanded: Boolean) =
        store.accept(StockStore.Intent.ToggleSearch(isExpanded))

    override fun executeSearch() =
        store.accept(StockStore.Intent.ExecuteSearch)

    override fun addToken(token: SearchToken) =
        store.accept(StockStore.Intent.AddToken(token))

    override fun removeToken(token: SearchToken) =
        store.accept(StockStore.Intent.RemoveToken(token))
}