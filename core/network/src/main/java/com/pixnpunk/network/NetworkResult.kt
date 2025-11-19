package com.pixnpunk.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

suspend inline fun <reified T> HttpClient.fetch(
    block: HttpRequestBuilder.() -> Unit
): Result<T> = try {
    val response = request(block)
    if (response.status == HttpStatusCode.OK)
        Result.success(response.body())
    else
        Result.failure(Throwable("${response.status}: ${response.bodyAsText()}"))
} catch (e: Exception) {
    Result.failure(e)
}

suspend inline fun <reified T> HttpClient.fetchForGet(
    url: String,
): Result<T> = try {
    val response = get(url)
    if (response.status == HttpStatusCode.OK)
        Result.success(response.body())
    else
        Result.failure(Throwable("${response.status}: ${response.bodyAsText()}"))
} catch (e: Exception) {
    Result.failure(e)
}

suspend inline fun <reified T> HttpClient.fetchForGet(
    baseUrl: String,
    apiPath: String,
    encodedApiPath: String,
    apiQuery: String,
    token: String
): Result<T> = try {
    val response = get(baseUrl) {
        url {
            appendPathSegments(apiPath, encodeSlash = false)
            appendPathSegments(encodedApiPath, encodeSlash = true)
            parameters.append("ref", apiQuery)
            parameters.append("token", token)
        }
    }
    if (response.status == HttpStatusCode.OK)
        Result.success(response.body())
    else
        Result.failure(Throwable("${response.status}: ${response.bodyAsText()}"))
} catch (e: Exception) {
    Result.failure(e)
}

inline fun <reified R> Result<R>.takeIfSuccess(): R? =
    if (this.isSuccess) this.getOrNull() else null

inline fun <reified R> Result<R>.handle(
    onSuccess: (R) -> Unit,
    onError: (Throwable) -> Unit
): Unit = if (this.isSuccess) {
    try {
        val value = getOrThrow()
        onSuccess(value)
    } catch (e: Exception) {
        onError(e)
    }
} else {
    onError(this.exceptionOrNull() ?: Throwable())
}

inline fun <reified R, reified T> Result<R>.handleMap(
    onSuccess: (R) -> T,
    onError: (Throwable) -> T
): T = if (this.isSuccess) {
    try {
        val value = getOrThrow()
        onSuccess(value)
    } catch (e: Exception) {
        onError(e)
    }
} else {
    onError(this.exceptionOrNull() ?: Throwable())
}

inline fun <reified R, reified T> Result<R>.map(mapper: (R) -> T): Result<T> =
    if (this.isSuccess) {
        try {
            val value = getOrThrow()
            Result.success(mapper(value))
        } catch (e: Exception) {
            Result.failure(e)
        }
    } else {
        Result.failure(this.exceptionOrNull() ?: Throwable())
    }