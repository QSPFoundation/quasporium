package com.pixnpunk.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkUtilTest {

    @Test
    fun `the host is the part a browser would open`() {
        assertEquals("qsp.org", "https://qsp.org/forum?topic=1#post".makeLinkHost())
        assertEquals("qsp.org", "HTTP://QSP.org:8080/".makeLinkHost())
        assertEquals("evil.example", "https://qsp.org@evil.example/login".makeLinkHost())
        assertEquals("evil.example", "https://qsp.org:pass@evil.example:443".makeLinkHost())
        assertEquals("[::1]", "http://[::1]:8080/a".makeLinkHost())
    }

    @Test
    fun `a host with look-alike letters is shown in ASCII`() {
        // The first letter is the Cyrillic "а"
        assertEquals("xn--pple-43d.com", "https://аpple.com/".makeLinkHost())
        assertEquals("xn--e1afmkfd.xn--p1ai", "https://пример.рф/страница".makeLinkHost())
    }

    @Test
    fun `extra slashes and backslashes are read as a browser reads them`() {
        assertEquals("path", "https:///path".makeLinkHost())
        assertEquals("qsp.org", "https:\\\\qsp.org\\forum".makeLinkHost())
        assertEquals("qsp.org", "https://qsp.org\\@evil.example/".makeLinkHost())
        assertEquals("qsp.org", "  https://qsp.org/  ".makeLinkHost())
    }

    @Test
    fun `an address without a host has none`() {
        assertNull("https://".makeLinkHost())
        assertNull("mailto:player@qsp.org".makeLinkHost())
        assertNull("not a link".makeLinkHost())
    }

    @Test
    fun `the prompt shows the host and the address`() {
        assertEquals(
            "Open a link from the game?<br><br><b>qsp.org</b><br><br>https://qsp.org/forum",
            "https://qsp.org/forum".makeLinkPrompt()
        )
    }

    @Test
    fun `markup in the address is shown as text`() {
        "https://a.example/?q=<b>&x='\"<a href=\"b\">".makeLinkPrompt().let {
            assertFalse(
                it,
                it.substringAfter("</b><br><br>").contains('<')
            )
            assertTrue(
                it,
                it.endsWith("?q=&lt;b&gt;&amp;x=&#39;&quot;&lt;a href=&quot;b&quot;&gt;")
            )
        }
    }

    @Test
    fun `a long address is cut`() {
        ("https://a.example/" + "x".repeat(500)).makeLinkPrompt().let {
            assertTrue(it, it.contains("<b>a.example</b>"))
            assertTrue(it, it.endsWith("x…"))
            assertEquals(200 + 1, it.substringAfterLast("<br>").length)
        }
    }
}
