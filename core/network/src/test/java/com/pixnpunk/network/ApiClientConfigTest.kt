package com.pixnpunk.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class ApiClientConfigTest {

    private companion object {
        const val URL = "https://qsp.org/api/v1/games"
    }

    private var requests = 0

    private fun client(vararg replies: MockRequestHandleScope.() -> HttpResponseData) =
        HttpClient(
            MockEngine { replies[requests++].invoke(this) }
        ) {
            installApiDefaults()
            install(HttpRequestRetry) { delay { } }
        }

    private fun MockRequestHandleScope.status(code: HttpStatusCode) = respond("", code)

    @Test
    fun retriesServerErrors() = runBlocking {
        val response = client(
            { status(HttpStatusCode.ServiceUnavailable) },
            { status(HttpStatusCode.OK) }
        ).get(URL)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(2, requests)
    }

    @Test
    fun retriesTooManyRequests() = runBlocking {
        val response = client(
            { status(HttpStatusCode.TooManyRequests) },
            { status(HttpStatusCode.OK) }
        ).get(URL)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(2, requests)
    }

    @Test
    fun givesUpAfterTwoRetries() {
        val client = client(
            { status(HttpStatusCode.BadGateway) },
            { status(HttpStatusCode.BadGateway) },
            { status(HttpStatusCode.BadGateway) }
        )

        val error = assertThrows(ServerResponseException::class.java) {
            runBlocking { client.get(URL) }
        }

        assertEquals(HttpStatusCode.BadGateway, error.response.status)
        assertEquals(3, requests)
    }

    @Test
    fun doesNotRetryNotFound() {
        val client = client(
            { status(HttpStatusCode.NotFound) },
            { status(HttpStatusCode.OK) }
        )

        assertThrows(ClientRequestException::class.java) {
            runBlocking { client.get(URL) }
        }
        assertEquals(1, requests)
    }

    @Test
    fun retriesConnectionFailures() = runBlocking {
        val response = client(
            { throw IOException("Connection reset") },
            { status(HttpStatusCode.OK) }
        ).get(URL)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(2, requests)
    }

    @Test
    fun doesNotRetryTimeouts() {
        val client = client(
            { throw SocketTimeoutException("Read timed out") },
            { status(HttpStatusCode.OK) }
        )

        assertThrows(SocketTimeoutException::class.java) {
            runBlocking { client.get(URL) }
        }
        assertEquals(1, requests)
    }

    @Test
    fun doesNotRetryPost() {
        val client = client(
            { status(HttpStatusCode.ServiceUnavailable) },
            { status(HttpStatusCode.OK) }
        )

        assertThrows(ServerResponseException::class.java) {
            runBlocking { client.post(URL) }
        }
        assertEquals(1, requests)
    }
}
