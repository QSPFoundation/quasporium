package com.pixnpunk.stock.data.repository

import androidx.paging.PagingSource
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.domain.model.NetworkError
import com.pixnpunk.stock.domain.model.NetworkException
import com.pixnpunk.stock.domain.model.SortBy
import com.pixnpunk.stock.domain.model.SortOrder
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.UnknownHostException

class RemotePagingSourceErrorTest {

    private fun loadError(handler: MockRequestHandler): NetworkError =
        runBlocking {
            val result = RemotePagingSource(
                ktorClient =
                    HttpClient(
                        engine = MockEngine(handler)
                    ) {
                        expectSuccess = true
                        install(ContentNegotiation) { json() }
                    },
                sortBy = SortBy.EMPTY,
                sortOrder = SortOrder.DESCENDING
            ).load(
                params = PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 30,
                    placeholdersEnabled = false
                )
            )

            val error = (result as PagingSource.LoadResult.Error<Int, GameEntry>).throwable
            (error as NetworkException).error
        }

    @Test
    fun serverErrorIsReportedWithItsCode() {
        assertEquals(
            NetworkError.Server(500),
            loadError {
                respond(
                    "<html>Internal Server Error</html>",
                    HttpStatusCode.InternalServerError
                )
            }
        )
    }

    @Test
    fun offlineIsReportedAsNoConnection() {
        assertEquals(
            NetworkError.NoConnection,
            loadError { throw UnknownHostException("qsp.org") }
        )
    }

    @Test
    fun malformedPageIsUnexpected() {
        assertEquals(
            NetworkError.Unexpected,
            loadError {
                respond(
                    "{\"unexpected\": true}",
                    HttpStatusCode.OK,
                    headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
        )
    }
}
