package com.pixnpunk.network

import coil3.ImageLoader
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.Dispatchers
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

private val imagesHttpClient = named("imagesHttpClient")

fun networkModule(userAgent: String): Module = module {
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .cache(
                Cache(
                    File(androidContext().cacheDir, "http_cache"),
                    50L * 1024L * 1024L
                )
            )
            .addInterceptor(UserAgentInterceptor(userAgent))
            .build()
    }

    single<HttpClient> {
        HttpClient(OkHttp) {
            engine {
                preconfigured = get()
                dispatcher = Dispatchers.IO
            }
            installApiDefaults()
        }
    }

    single<HttpClient>(imagesHttpClient) { imageHttpClient(get()) }

    single<ImageLoader> {
        imageLoader(androidContext()) { get<HttpClient>(imagesHttpClient) }
    }
}
