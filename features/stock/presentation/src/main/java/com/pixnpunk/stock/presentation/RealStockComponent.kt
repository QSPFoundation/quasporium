package com.pixnpunk.stock.presentation

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.GameListType
import com.pixnpunk.stock.domain.usecase.FetchRemoteDescUseCase
import com.pixnpunk.stock.presentation.StockComponent.Child
import com.pixnpunk.stock.presentation.mvi.RealStockStore
import com.pixnpunk.stock.presentation.mvi.StockStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealStockComponent(
    private val componentContext: ComponentContext,
    override val showDialogAdd: () -> Unit,
    val navigateToDetails: (GameEntry) -> Unit,
    override val navigateToSettings: () -> Unit
) : ComponentContext by componentContext, StockComponent, KoinComponent {

    private val sourceViewModel: SourceViewModel = get()
    private val outScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val contentNavigation = StackNavigation<ChildConfig>()

    private val store = instanceKeeper.getStore {
        RealStockStore(DefaultStoreFactory()).create()
    }

    init {
        sourceViewModel.loadLocalData()
        sourceViewModel.loadRemoteData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<StockStore.State> = store.stateFlow
    override val gameRemotePageFlow: Flow<PagingData<GameEntry>> = sourceViewModel.remotePagingFlow
    override val gameLocalPageFlow: Flow<PagingData<GameEntry>> = sourceViewModel.localPagingFlow
    override val childStack: Value<ChildStack<*, Child>> =
        childStack(
            key = "contentChildStack",
            source = contentNavigation,
            serializer = serializer(),
            initialConfiguration = ChildConfig.Installed,
            handleBackButton = true,
            childFactory = { config, _ ->
                when (config) {
                    is ChildConfig.Installed -> {
                        Child.InstalledChild()
                    }

                    is ChildConfig.Library -> {
                        Child.LibraryChild()
                    }
                }
            }
        )

    override fun startDeleteMode() {
        store.accept(StockStore.Intent.StartDeleteMode)
    }

    override fun stopDeleteMode() {
        store.accept(StockStore.Intent.StopDeleteMode)
    }

    override fun deleteEntries(entries: Set<GameEntry>) {
        store.accept(StockStore.Intent.DeleteEntries(entries))
    }

    override fun searchEntries(query: String) {
        sourceViewModel.searchGameEntry(query)
    }

    override fun navigateToDetails(entry: GameEntry) {
        if (entry.listId == GameListType.Playable) {
            navigateToDetails.invoke(entry)
        } else {
            outScope.launch {
                val desc = get<FetchRemoteDescUseCase>().execute(entry.descUrl)

                withContext(Dispatchers.Main) {
                    this@RealStockComponent.navigateToDetails.invoke(entry.copy(descUrl = desc))
                }
            }
        }
    }

    override fun navigateTo(config: ChildConfig) {
        contentNavigation.bringToFront(config)
    }

    @Serializable
    sealed class ChildConfig {
        @Serializable
        data object Installed : ChildConfig()

        @Serializable
        data object Library : ChildConfig()
    }
}