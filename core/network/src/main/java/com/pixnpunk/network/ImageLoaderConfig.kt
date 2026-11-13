package com.pixnpunk.network

import android.content.Context
import android.os.Build
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.serviceLoaderEnabled
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient

private const val IMAGE_CACHE_DIR = "image_cache"
internal const val IMAGE_DISK_CACHE_BYTES = 64L * 1024 * 1024

internal fun OkHttpClient.withoutHttpCache(): OkHttpClient =
    newBuilder().cache(null).build()

internal fun imageHttpClient(
    okHttpClient: OkHttpClient
): HttpClient =
    HttpClient(OkHttp) {
        engine {
            preconfigured = okHttpClient.withoutHttpCache()
            dispatcher = Dispatchers.IO
        }
        install(HttpTimeout) {
            connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
            socketTimeoutMillis = TIMEOUT_MILLIS
        }
    }

internal fun imageLoader(
    context: Context,
    httpClient: () -> HttpClient
): ImageLoader =
    ImageLoader.Builder(context)
        .serviceLoaderEnabled(false)
        .components {
            add(KtorNetworkFetcherFactory(httpClient))
            add(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) AnimatedImageDecoder.Factory()
                else GifDecoder.Factory()
            )
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve(IMAGE_CACHE_DIR))
                .maxSizeBytes(IMAGE_DISK_CACHE_BYTES)
                .build()
        }
        .build()
