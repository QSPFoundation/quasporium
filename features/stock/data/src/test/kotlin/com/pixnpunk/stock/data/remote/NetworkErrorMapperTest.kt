package com.pixnpunk.stock.data.remote

import com.pixnpunk.stock.data.mapper.toNetworkError
import com.pixnpunk.stock.domain.model.NetworkError
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.network.sockets.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkErrorMapperTest {

    private fun responseError(
        code: HttpStatusCode
    ): Throwable =
        runBlocking {
            runCatching {
                HttpClient(
                    engine = MockEngine { respond("", code) }
                ) { expectSuccess = true }.get("https://qsp.org/api/v1/games")
            }.exceptionOrNull()!!
        }

    @Test
    fun connectionFailuresMeanNoConnection() {
        listOf(
            UnknownHostException("qsp.org"),
            ConnectException("Failed to connect"),
            IOException("unexpected end of stream")
        ).forEach { assertEquals(NetworkError.NoConnection, it.toNetworkError()) }
    }

    @Test
    fun timeoutsMeanTimeout() {
        listOf(
            ConnectTimeoutException("Connect timeout"),
            SocketTimeoutException("Read timed out"),
            responseError(HttpStatusCode.RequestTimeout)
        ).forEach { assertEquals(NetworkError.Timeout, it.toNetworkError()) }
    }

    @Test
    fun missingResourcesMeanNotFound() {
        assertEquals(NetworkError.NotFound, responseError(HttpStatusCode.NotFound).toNetworkError())
        assertEquals(NetworkError.NotFound, responseError(HttpStatusCode.Gone).toNetworkError())
    }

    @Test
    fun otherStatusesKeepTheirCode() {
        assertEquals(
            NetworkError.Server(503),
            responseError(HttpStatusCode.ServiceUnavailable).toNetworkError()
        )
        assertEquals(
            NetworkError.Server(429),
            responseError(HttpStatusCode.TooManyRequests).toNetworkError()
        )
    }

    @Test
    fun malformedResponsesAreUnexpected() {
        assertEquals(
            NetworkError.Unexpected,
            SerializationException("Unexpected JSON token").toNetworkError()
        )
    }
}
