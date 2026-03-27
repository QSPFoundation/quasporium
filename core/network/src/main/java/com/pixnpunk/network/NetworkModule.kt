package com.pixnpunk.network

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.io.File

val networkModule = module {
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .cache(
                Cache(
                    File(androidContext().cacheDir, "http_cache"),
                    50L * 1024L * 1024L
                )
            )
            .build()
    }

    single<HttpClient> {
        HttpClient(OkHttp) {
            engine {
                preconfigured = get()
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
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
    }
}