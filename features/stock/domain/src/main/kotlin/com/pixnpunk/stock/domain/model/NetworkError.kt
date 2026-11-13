package com.pixnpunk.stock.domain.model

sealed interface NetworkError {
    data object NoConnection : NetworkError
    data object Timeout : NetworkError
    data object NotFound : NetworkError
    data class Server(val code: Int) : NetworkError
    data object Unexpected : NetworkError
}

class NetworkException(
    val error: NetworkError,
    cause: Throwable
) : Exception(cause.message, cause)
