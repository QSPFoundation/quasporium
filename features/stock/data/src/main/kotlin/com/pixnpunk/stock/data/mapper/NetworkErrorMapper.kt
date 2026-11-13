package com.pixnpunk.stock.data.mapper

import com.pixnpunk.stock.domain.model.NetworkError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import java.io.IOException
import java.net.SocketTimeoutException

internal fun Throwable.toNetworkError(): NetworkError =
    when (this) {
        is HttpRequestTimeoutException,
        is ConnectTimeoutException,
        is SocketTimeoutException -> NetworkError.Timeout

        is ResponseException -> when (response.status) {
            HttpStatusCode.NotFound, HttpStatusCode.Gone -> NetworkError.NotFound
            HttpStatusCode.RequestTimeout -> NetworkError.Timeout
            else -> NetworkError.Server(response.status.value)
        }

        is IOException -> NetworkError.NoConnection
        else -> NetworkError.Unexpected
    }
