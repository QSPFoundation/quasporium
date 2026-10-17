package com.pixnpunk.stock.presentation

import com.pixnpunk.stock.domain.model.SearchState
import com.pixnpunk.stock.domain.model.SortOrder
import com.pixnpunk.stock.presentation.mvi.StockStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SearchStateFlowTest {

    private val state = MutableStateFlow(StockStore.State())

    @Test
    fun uiOnlyChangesDoNotEmit() = runTest {
        val emitted = mutableListOf<SearchState>()
        backgroundScope.launch { state.toSearchState().collect { emitted += it } }
        runCurrent()

        state.update { it.copy(isSearchExpanded = true) }
        state.update { it.copy(isDeleteModeActive = true, selectedGameIds = setOf(1, 2)) }
        state.update { it.copy(suggestedAuthors = listOf("Author")) }
        advanceTimeBy((SEARCH_DEBOUNCE_MS * 2).milliseconds)

        assertEquals(listOf(SearchState()), emitted)
    }

    @Test
    fun typingIsDebouncedIntoOneSearch() = runTest {
        val emitted = mutableListOf<SearchState>()
        backgroundScope.launch { state.toSearchState().collect { emitted += it } }
        runCurrent()

        "quest".indices.forEach { end ->
            state.update { it.copy(searchQuery = "quest".substring(0, end + 1)) }
            advanceTimeBy((SEARCH_DEBOUNCE_MS / 4).milliseconds)
        }
        advanceTimeBy((SEARCH_DEBOUNCE_MS + 1).milliseconds)

        assertEquals(listOf("", "quest"), emitted.map { it.query })
    }

    @Test
    fun sortAndClearingTheQueryApplyAtOnce() = runTest {
        val emitted = mutableListOf<SearchState>()
        backgroundScope.launch { state.toSearchState().collect { emitted += it } }
        state.update { it.copy(searchQuery = "quest") }
        advanceTimeBy((SEARCH_DEBOUNCE_MS + 1).milliseconds)

        state.update { it.copy(sortOrder = SortOrder.ASCENDING) }
        runCurrent()
        assertEquals(SortOrder.ASCENDING, emitted.last().sortOrder)

        state.update { it.copy(searchQuery = "") }
        runCurrent()
        assertEquals("", emitted.last().query)
    }
}
