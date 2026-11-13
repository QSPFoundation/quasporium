package com.pixnpunk.network

import io.ktor.client.HttpClientConfig
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

private const val MAX_RETRIES = 2
private const val TIMEOUT_MILLIS = 15_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L
private const val FIRST_SERVER_ERROR = 500

private val transientStatuses = setOf(
    HttpStatusCode.RequestTimeout,
    HttpStatusCode.TooManyRequests
)

private fun HttpStatusCode.isTransient(): Boolean =
    value >= FIRST_SERVER_ERROR || this in transientStatuses

private fun Throwable.isTimeout(): Boolean =
    this is HttpRequestTimeoutException ||
        this is ConnectTimeoutException ||
        this is java.net.SocketTimeoutException

internal fun HttpClientConfig<*>.installApiDefaults() {
    expectSuccess = true

    install(HttpTimeout) {
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
        socketTimeoutMillis = TIMEOUT_MILLIS
        requestTimeoutMillis = TIMEOUT_MILLIS
    }

    install(HttpRequestRetry) {
        retryIf(MAX_RETRIES) { request, response ->
            request.method == HttpMethod.Get && response.status.isTransient()
        }
        retryOnExceptionIf(MAX_RETRIES) { request, cause ->
            request.method == HttpMethod.Get &&
                cause !is CancellationException &&
                !cause.isTimeout()
        }
        exponentialDelay(
            baseDelayMs = 1_000,
            maxDelayMs = 4_000,
            randomizationMs = 500
        )
    }

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
        )
    }
}
