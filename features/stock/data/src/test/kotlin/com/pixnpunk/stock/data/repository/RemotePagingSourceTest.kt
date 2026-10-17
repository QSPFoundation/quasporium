package com.pixnpunk.stock.data.repository

import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.INITIAL_PAGE_INDEX
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.PAGE_SIZE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemotePagingSourceTest {

    @Test
    fun loadsExactlyTheApiPages() {
        val totalGames = 307
        val lastPage = (totalGames + PAGE_SIZE - 1) / PAGE_SIZE

        val requestedPages = mutableListOf<Int>()
        var loadedGames = 0
        var page: Int? = INITIAL_PAGE_INDEX
        while (page != null) {
            requestedPages += page
            val gamesOnPage = (totalGames - (page - 1) * PAGE_SIZE).coerceIn(0, PAGE_SIZE)
            loadedGames += gamesOnPage
            page = nextPageKey(page, lastPage, isPageEmpty = gamesOnPage == 0)
        }

        assertEquals((INITIAL_PAGE_INDEX..lastPage).toList(), requestedPages)
        assertEquals(totalGames, loadedGames)
    }

    @Test
    fun emptyPageEndsPaging() {
        assertNull(nextPageKey(currentPage = 3, lastPage = 10, isPageEmpty = true))
    }

    @Test
    fun singlePageHasNoNextKey() {
        assertNull(nextPageKey(currentPage = INITIAL_PAGE_INDEX, lastPage = 1, isPageEmpty = false))
    }
}
