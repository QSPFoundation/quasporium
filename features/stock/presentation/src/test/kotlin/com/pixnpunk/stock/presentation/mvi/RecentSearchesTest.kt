package com.pixnpunk.stock.presentation.mvi

import com.pixnpunk.stock.domain.model.SearchTokenType
import com.pixnpunk.stock.presentation.model.SearchTokenUI
import org.junit.Assert.assertEquals
import org.junit.Test

class RecentSearchesTest {

    private fun title(query: String) = SearchTokenUI(query, SearchTokenType.TITLE, query)

    @Test
    fun addsNewSearchToTheEnd() {
        assertEquals(
            listOf(title("abc"), title("quest")),
            listOf(title("abc")).withRecentSearch(title("quest"))
        )
    }

    @Test
    fun movesRepeatedSearchToTheEndWithoutDuplicates() {
        assertEquals(
            listOf(title("def"), title("abc")),
            listOf(title("abc"), title("def")).withRecentSearch(title("abc"))
        )
    }

    @Test
    fun keepsOnlyTheLastSearches() {
        val recent = (1..MAX_RECENT_SEARCHES).map { title("query$it") }
        val updated = recent.withRecentSearch(title("new"))
        assertEquals(MAX_RECENT_SEARCHES, updated.size)
        assertEquals(title("query2"), updated.first())
        assertEquals(title("new"), updated.last())
    }
}
