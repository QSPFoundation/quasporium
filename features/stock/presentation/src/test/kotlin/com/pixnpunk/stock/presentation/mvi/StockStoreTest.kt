package com.pixnpunk.stock.presentation.mvi

import com.arkivanov.mvikotlin.core.utils.isAssertOnMainThreadEnabled
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.stock.domain.model.SearchTokenType
import com.pixnpunk.stock.domain.model.SortOrder
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.stock.domain.usecase.DeleteLocalGameEntriesUseCase
import com.pixnpunk.stock.presentation.model.SearchTokenUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy

@OptIn(ExperimentalCoroutinesApi::class)
class StockStoreTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val deleted = mutableListOf<Set<Long>>()

    private val localRepository = object : LocalRepository by unimplemented() {
        override suspend fun deleteGameEntries(idsDelete: Set<Long>): Int {
            deleted += idsDelete
            return idsDelete.size
        }
    }

    private val logger = object : AppLogger {
        override fun recordBreadcrumb(message: String) = Unit
        override fun recordNonFatalException(throwable: Throwable) = Unit
    }

    private lateinit var store: StockStore

    @Before
    fun setUp() {
        isAssertOnMainThreadEnabled = false
        Dispatchers.setMain(dispatcher)
        store = RealStockStore(DefaultStoreFactory()) {
            StockExecutor(
                deleteLocalGameEntriesUseCase = DeleteLocalGameEntriesUseCase(localRepository),
                logger = logger,
                mainContext = dispatcher,
                ioContext = dispatcher
            )
        }.create()
    }

    @After
    fun tearDown() {
        store.dispose()
        Dispatchers.resetMain()
        isAssertOnMainThreadEnabled = true
    }

    @Test
    fun `selection toggles game ids`() {
        store.accept(StockStore.Intent.EnterDeleteMode)
        store.accept(StockStore.Intent.ToggleGameSelection(1))
        store.accept(StockStore.Intent.ToggleGameSelection(2))
        store.accept(StockStore.Intent.ToggleGameSelection(1))

        assertTrue(store.state.isDeleteModeActive)
        assertEquals(setOf(2L), store.state.selectedGameIds)
    }

    @Test
    fun `deleting selected games clears selection and leaves delete mode`() {
        store.accept(StockStore.Intent.EnterDeleteMode)
        store.accept(StockStore.Intent.AddAllVisibleGames(setOf(1, 2, 3)))
        store.accept(StockStore.Intent.DeleteSelectedGames)

        assertEquals(listOf(setOf(1L, 2L, 3L)), deleted)
        assertTrue(store.state.selectedGameIds.isEmpty())
        assertFalse(store.state.isDeleteModeActive)
    }

    @Test
    fun `nothing is deleted without a selection`() {
        store.accept(StockStore.Intent.EnterDeleteMode)
        store.accept(StockStore.Intent.DeleteSelectedGames)

        assertTrue(deleted.isEmpty())
        assertTrue(store.state.isDeleteModeActive)
    }

    @Test
    fun `exit delete mode clears selection`() {
        store.accept(StockStore.Intent.EnterDeleteMode)
        store.accept(StockStore.Intent.ToggleGameSelection(5))
        store.accept(StockStore.Intent.ExitDeleteMode)

        assertTrue(store.state.selectedGameIds.isEmpty())
        assertFalse(store.state.isDeleteModeActive)
    }

    @Test
    fun `sort order toggles`() {
        store.accept(StockStore.Intent.ToggleSortOrder)
        assertEquals(SortOrder.ASCENDING, store.state.sortOrder)

        store.accept(StockStore.Intent.ToggleSortOrder)
        assertEquals(SortOrder.DESCENDING, store.state.sortOrder)
    }

    @Test
    fun `short query is not remembered`() {
        store.accept(StockStore.Intent.ToggleSearch(true))
        store.accept(StockStore.Intent.UpdateQuery("ab"))
        store.accept(StockStore.Intent.ExecuteSearch)

        assertTrue(store.state.recentSearches.isEmpty())
        assertFalse(store.state.isSearchExpanded)
    }

    @Test
    fun `executed query becomes a recent title search`() {
        store.accept(StockStore.Intent.UpdateQuery("quest"))
        store.accept(StockStore.Intent.ExecuteSearch)

        assertEquals(
            listOf(SearchTokenUI("quest", SearchTokenType.TITLE, "quest")),
            store.state.recentSearches
        )
    }

    @Test
    fun `token is added once and clears the query`() {
        val token = SearchTokenUI("Author", SearchTokenType.AUTHOR, "Author")
        store.accept(StockStore.Intent.UpdateQuery("auth"))
        store.accept(StockStore.Intent.AddToken(token))
        store.accept(StockStore.Intent.AddToken(token))

        assertEquals(listOf(token), store.state.activeTokens)
        assertEquals("", store.state.searchQuery)

        store.accept(StockStore.Intent.RemoveToken(token))
        assertTrue(store.state.activeTokens.isEmpty())
    }
}

/** A stand-in for an interface whose members the test does not expect to be called. */
private inline fun <reified T : Any> unimplemented(): T =
    Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
        throw UnsupportedOperationException("${T::class.simpleName}.${method.name} is not expected in this test")
    } as T
