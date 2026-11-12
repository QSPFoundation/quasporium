package com.pixnpunk.webview.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class BottomInsetTest {
    private val page = "<html><head><style>:root{--qsp-bottom-inset: 0px;}</style></head><body></head></body></html>"

    @Test
    fun `the inset is set in the head of the page once`() {
        assertEquals(
            "<html><head><style>:root{--qsp-bottom-inset: 0px;}</style>" +
                "<style>:root{--qsp-bottom-inset:56px}</style></head><body></head></body></html>",
            page.withBottomInset(56)
        )
    }

    @Test
    fun `a page without an inset is left as it is`() {
        assertEquals(page, page.withBottomInset(0))
        assertEquals(page, page.withBottomInset(-1))
    }

    @Test
    fun `the script changes the inset of a loaded page`() {
        assertEquals(
            "document.documentElement.style.setProperty('--qsp-bottom-inset', '56px');",
            bottomInsetScript(56)
        )
        assertEquals(
            "document.documentElement.style.setProperty('--qsp-bottom-inset', '0px');",
            bottomInsetScript(-3)
        )
    }
}
