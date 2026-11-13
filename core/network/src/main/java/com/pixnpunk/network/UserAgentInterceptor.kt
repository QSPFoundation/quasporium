package com.pixnpunk.network

import io.ktor.http.HttpHeaders
import okhttp3.Interceptor
import okhttp3.Response

internal class UserAgentInterceptor(
    private val userAgent: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(
            chain.request().newBuilder()
                .header(HttpHeaders.UserAgent, userAgent)
                .build()
        )
}
