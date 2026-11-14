package com.pixnpunk.stock.data.repository

import androidx.paging.PagingSource
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.GameListType
import com.pixnpunk.stock.domain.model.SortBy
import com.pixnpunk.stock.domain.model.SortOrder
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteGamesPageTest {

    private val page = requireNotNull(javaClass.getResource("/games-page.json")).readText()

    private fun load(): PagingSource.LoadResult.Page<Int, GameEntry> = runBlocking {
        val client = HttpClient(
            MockEngine {
                respond(
                    page,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
        ) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    }
                )
            }
        }

        RemotePagingSource(client, sortBy = SortBy.EMPTY, sortOrder = SortOrder.DESCENDING)
            .load(PagingSource.LoadParams.Refresh(null, 30, false))
            as PagingSource.LoadResult.Page<Int, GameEntry>
    }

    @Test
    fun catalogPageIsParsedIntoGameEntries() {
        val result = load()

        assertEquals(listOf("SE A JEA", "Царица Белых Кровей"), result.data.map { it.title })
        assertNull(result.prevKey)
        assertEquals(2, result.nextKey)

        val game = result.data.first()
        assertEquals(332L, game.id)
        assertEquals(GameListType.Downloadable, game.listId)
        assertEquals("Mkir", game.author)
        assertEquals("1.0", game.version)
        assertEquals("ru", game.lang)
        assertEquals(542128L, game.fileSize)
        assertEquals("https://qsp.org/games/332-se-a-jea/download", game.fileUrl)
        assertEquals("https://qsp.org/storage/games/332/ковер.jpg", game.gameIconPath)
        assertEquals("2026-11-10T17:23:24+00:00", game.pubDate)
    }
}
