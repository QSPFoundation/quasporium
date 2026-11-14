package com.pixnpunk.stock.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.arkivanov.mvikotlin.core.utils.isAssertOnMainThreadEnabled
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.stock.domain.model.SearchTokenType
import com.pixnpunk.stock.domain.model.SortBy
import com.pixnpunk.stock.domain.model.SortOrder
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.stock.domain.repository.RemoteRepository
import com.pixnpunk.stock.domain.usecase.DeleteLocalGameEntriesUseCase
import com.pixnpunk.stock.domain.usecase.GetAuthorSuggestionUseCase
import com.pixnpunk.stock.domain.usecase.GetLocalGameEntriesUseCase
import com.pixnpunk.stock.domain.usecase.GetRemoteGameEntriesExcludingLocalUseCase
import com.pixnpunk.stock.domain.usecase.GetTranslatorSuggestionUseCase
import com.pixnpunk.stock.presentation.model.SearchTokenUI
import com.pixnpunk.stock.presentation.mvi.StockExecutor
import com.pixnpunk.stock.presentation.mvi.StockStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.lang.reflect.Proxy

@OptIn(ExperimentalCoroutinesApi::class)
class RealStockComponentStateTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val localRepository = unimplemented<LocalRepository>()
    private val remoteRepository = unimplemented<RemoteRepository>()

    private val appLogger = object : AppLogger {
        override fun recordBreadcrumb(message: String) = Unit
        override fun recordNonFatalException(throwable: Throwable) = Unit
    }

    private val authorToken = SearchTokenUI(
        "Mkir",
        SearchTokenType.AUTHOR,
        "Mkir"
    )

    private fun component(stateKeeper: StateKeeperDispatcher) =
        RealStockComponent(
            componentContext = DefaultComponentContext(
                lifecycle = LifecycleRegistry(),
                stateKeeper = stateKeeper
            ),
            showDialogAdd = {},
            navigateToSettings = {},
            navigateToDetails = {},
            settingsState = MutableStateFlow(SettingsEntity())
        )

    private fun SerializableContainer.afterProcessDeath(): SerializableContainer =
        Json.decodeFromString(
            SerializableContainer.serializer(),
            Json.encodeToString(SerializableContainer.serializer(), this)
        )

    @Before
    fun setUp() {
        isAssertOnMainThreadEnabled = false
        Dispatchers.setMain(dispatcher)
        startKoin {
            modules(
                module {
                    factory {
                        StockExecutor(
                            deleteLocalGameEntriesUseCase = DeleteLocalGameEntriesUseCase(localRepository),
                            logger = appLogger,
                            mainContext = dispatcher,
                            ioContext = dispatcher
                        )
                    }
                    factory { GetLocalGameEntriesUseCase(localRepository) }
                    factory { GetRemoteGameEntriesExcludingLocalUseCase(remoteRepository, localRepository) }
                    factory { GetAuthorSuggestionUseCase(remoteRepository) }
                    factory { GetTranslatorSuggestionUseCase(remoteRepository) }
                }
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
        isAssertOnMainThreadEnabled = true
    }

    @Test
    fun searchFiltersAndRecentSearchesSurviveProcessDeath() {
        val stateKeeper = StateKeeperDispatcher()
        component(stateKeeper).apply {
            addToken(authorToken)
            changeSortBy(SortBy.LIKES)
            changeSortOrder()
            changeSortLang("ru")
            updateSearchQuery("dragon")
        }

        val restored = component(
            StateKeeperDispatcher(stateKeeper.save().afterProcessDeath())
        ).model.value

        assertEquals("dragon", restored.searchQuery)
        assertEquals(listOf(authorToken), restored.activeTokens)
        assertEquals(listOf(authorToken), restored.recentSearches)
        assertEquals(SortBy.LIKES, restored.sortBy)
        assertEquals(SortOrder.ASCENDING, restored.sortOrder)
        assertEquals("ru", restored.selectedLang)
    }

    @Test
    fun firstStartBeginsWithDefaults() {
        assertEquals(
            StockStore.State(),
            component(StateKeeperDispatcher()).model.value
        )
    }
}

private inline fun <reified T : Any> unimplemented(): T =
    Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
        throw UnsupportedOperationException("${T::class.simpleName}.${method.name} is not expected in this test")
    } as T
