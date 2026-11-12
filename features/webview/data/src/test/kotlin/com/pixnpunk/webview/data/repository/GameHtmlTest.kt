package com.pixnpunk.webview.data.repository

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameHtmlTest {

    private fun body(html: String) =
        Jsoup.parseBodyFragment(html).apply { outputSettings().prettyPrint(false) }

    @Test
    fun `plain text keeps markup, spaces and line breaks as text`() {
        val raw = "a < b && c > d\n<b>bold</b>  'quoted' \"too\"\n\tend"
        val document = body(raw.plainTextBody())

        assertEquals(raw, document.body().wholeText())
        assertTrue(document.body().select("b").isEmpty())
    }

    @Test
    fun `plain text cannot add a script or an event handler`() {
        assertTrue(
            body(
                "<script>alert(1)</script><img src=x onerror=alert(2)>".plainTextBody()
            ).select("script, img").isEmpty()
        )
    }

    @Test
    fun `page scripts are forbidden before anything else in the head`() {
        val document = Jsoup.parse("<html><head><script>1</script></head><body></body></html>")

        document.forbidPageScripts()

        val first = document.head().child(0)
        assertEquals("meta", first.tagName())
        assertEquals("Content-Security-Policy", first.attr("http-equiv"))
        assertEquals("script-src 'none'", first.attr("content"))
    }
}
