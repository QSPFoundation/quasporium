package com.pixnpunk.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.DrawableImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class ImageLoaderTest {

    private companion object {
        const val GIF =
            "R0lGODlhAQABAIAAAAAAAP///yH/C05FVFNDQVBFMi4wAwEAAAAh+QQACgAAACwAAAAAAQABAAACAkQBACH5" +
                    "BAAKAAAALAAAAAABAAEAAAICTAEAOw=="
    }

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val requestedUrls = mutableListOf<String>()

    private val png: ByteArray =
        ByteArrayOutputStream().also {
            Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }.toByteArray()

    private val loader = imageLoader(context) {
        HttpClient(
            MockEngine { request ->
                requestedUrls += request.url.toString()
                val gif = request.url.encodedPath.endsWith(".gif")
                respond(
                    if (gif) Base64.decode(GIF, Base64.DEFAULT) else png,
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        if (gif) "image/gif" else "image/png"
                    )
                )
            }
        )
    }

    private fun load(url: String) = runBlocking {
        loader.execute(
            ImageRequest.Builder(context)
                .data(url)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build()
        )
    }

    @After
    fun tearDown() {
        loader.shutdown()
        loader.diskCache?.clear()
    }

    @Test
    fun loadsCoversThroughTheGivenClient() {
        val result = load("https://qsp.org/storage/games/305/cover.jpg")

        assertTrue(result is SuccessResult)
        assertEquals(listOf("https://qsp.org/storage/games/305/cover.jpg"), requestedUrls)
    }

    @Test
    fun decodesGifsAsAnimations() {
        val result = load("https://qsp.org/storage/games/305/icon.gif") as SuccessResult

        assertTrue((result.image as? DrawableImage)?.drawable is Animatable)
    }

    @Test
    fun onlyOurNetworkFetcherIsRegistered() {
        val networkFetchers = loader.components.fetcherFactories
            .filter { (factory, _) -> "Network" in factory::class.java.name }

        assertEquals(1, networkFetchers.size)
    }

    @Test
    fun diskCacheHasAFixedSize() {
        assertEquals(IMAGE_DISK_CACHE_BYTES, loader.diskCache?.maxSize)
    }
}
