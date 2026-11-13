package com.pixnpunk.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class UserAgentInterceptorTest {

    private companion object {
        const val USER_AGENT = "Quasporium/1.15.0 (Android 14; foss)"
    }

    @Test
    fun replacesTheKtorUserAgent() = runBlocking {
        var sent: Request? = null
        val okHttp = OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor(USER_AGENT))
            .addInterceptor { chain ->
                sent = chain.request()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("".toResponseBody())
                    .build()
            }
            .build()

        HttpClient(OkHttp) { engine { preconfigured = okHttp } }
            .get("https://qsp.org/api/v1/games")

        assertEquals(USER_AGENT, sent?.header(HttpHeaders.UserAgent))
    }
}
