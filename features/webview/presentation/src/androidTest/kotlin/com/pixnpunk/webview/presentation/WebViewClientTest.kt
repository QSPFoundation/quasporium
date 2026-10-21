package com.pixnpunk.webview.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.webkit.WebViewAssetLoader
import com.pixnpunk.common.Base64Util.encodeBase64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebViewClientTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val execValues = mutableListOf<String>()
    private val startedIntents = mutableListOf<Intent>()

    private val client = WebViewClient(
        assetLoader = lazy { WebViewAssetLoader.Builder().build() },
        onExecValue = { execValues += it },
        onStartActivity = { startedIntents += it }
    )

    private fun request(url: String, hasGesture: Boolean = true) = object : WebResourceRequest {
        override fun getUrl(): Uri = Uri.parse(url)
        override fun isForMainFrame() = true
        override fun isRedirect() = false
        override fun hasGesture() = hasGesture
        override fun getMethod() = "GET"
        override fun getRequestHeaders() = emptyMap<String, String>()
    }

    @Test
    fun linksWithoutAGestureAreNotOpened() = onMainThread { view ->
        for (url in listOf("https://example.org", "http://example.org")) {
            assertTrue(url, client.shouldOverrideUrlLoading(view, request(url, hasGesture = false)))
        }

        assertEquals(emptyList<Intent>(), startedIntents)
    }

    @Test
    fun tappedLinksOpenInABrowser() = onMainThread { view ->
        assertTrue(client.shouldOverrideUrlLoading(view, request("https://example.org/a", hasGesture = true)))

        val intent = startedIntents.single()
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(Uri.parse("https://example.org/a"), intent.data)
    }

    @Test
    fun execLinksRunTheirCode() = onMainThread { view ->
        val code = "gt 'start'"

        assertTrue(client.shouldOverrideUrlLoading(view, request("exec:${code.encodeBase64()}")))

        assertEquals(listOf(code), execValues)
    }

    @Test
    fun otherSchemesAreNotLoaded() = onMainThread { view ->
        for (url in listOf("file:///sdcard/a.txt", "intent://scan/#Intent;end", "javascript:alert(1)")) {
            assertTrue(url, client.shouldOverrideUrlLoading(view, request(url, hasGesture = true)))
        }

        assertEquals(emptyList<Intent>(), startedIntents)
        assertEquals(emptyList<String>(), execValues)
    }

    private fun onMainThread(block: (WebView) -> Unit) {
        var failure: Throwable? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val view = WebView(context)
            try {
                block(view)
            } catch (e: Throwable) {
                failure = e
            } finally {
                view.destroy()
            }
        }
        failure?.let { throw it }
    }
}
