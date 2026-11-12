package com.pixnpunk.webview.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class PageScriptPolicyTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var webView: WebView

    private companion object {
        const val TIMEOUT_S = 10L
        const val BASE_URL = "https://appassets.androidplatform.net/game_files/"
        const val PAGE = """<html><head>
            <meta http-equiv="Content-Security-Policy" content="script-src 'none'">
            </head><body>
            <script>document.title = 'page script ran'</script>
            <img src="x" onerror="document.title = 'handler ran'">
            </body></html>"""
    }

    private fun evaluate(script: String): String? {
        val result = AtomicReference<String?>()
        val done = CountDownLatch(1)
        instrumentation.runOnMainSync {
            webView.evaluateJavascript(script) {
                result.set(it)
                done.countDown()
            }
        }
        assertTrue("script did not finish: $script", done.await(TIMEOUT_S, TimeUnit.SECONDS))
        return result.get()
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Before
    fun setUp() {
        val loaded = CountDownLatch(1)
        instrumentation.runOnMainSync {
            webView = WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        loaded.countDown()
                    }
                }
                loadDataWithBaseURL(BASE_URL, PAGE, "text/html", "UTF-8", null)
            }
        }
        assertTrue("page did not load", loaded.await(TIMEOUT_S, TimeUnit.SECONDS))
    }

    @After
    fun tearDown() {
        instrumentation.runOnMainSync { webView.destroy() }
    }

    @Test
    fun scriptsOfThePageDoNotRun() {
        assertEquals("\"\"", evaluate("document.title"))
    }

    @Test
    fun theAppCanStillChangeThePage() {
        val value = evaluate(
            "document.documentElement.style.setProperty('--qsp-bottom-inset', '56px');" +
                "document.documentElement.style.getPropertyValue('--qsp-bottom-inset')"
        )

        assertEquals("\"56px\"", value)
    }
}
