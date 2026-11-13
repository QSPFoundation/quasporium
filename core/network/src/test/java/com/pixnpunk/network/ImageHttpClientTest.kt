package com.pixnpunk.network

import okhttp3.Cache
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ImageHttpClientTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun imagesShareTheConnectionsAndUserAgentButNotTheHttpCache() {
        val shared = OkHttpClient.Builder()
            .cache(Cache(folder.root, 1024L))
            .addInterceptor(UserAgentInterceptor("Quasporium/1.15.0 (Android 14; foss)"))
            .build()

        val images = shared.withoutHttpCache()

        assertNull(images.cache)
        assertSame(shared.connectionPool, images.connectionPool)
        assertEquals(shared.interceptors, images.interceptors)
    }
}
