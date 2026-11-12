package com.pixnpunk.webview.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameImagePathTest {

    @Test
    fun `an image of the game page is a path in the game folder`() {
        assertEquals("images/pic one.png", gameImagePath("${GAME_FILES_URL}images/pic%20one.png"))
        assertEquals("images/a+b.png", gameImagePath("${GAME_FILES_URL}images/a+b.png"))
        assertEquals("images/pic.png", gameImagePath("${GAME_FILES_URL}images/pic.png?v=2#top"))
    }

    @Test
    fun `a relative source is kept as the game wrote it`() {
        assertEquals("images/pic.png", gameImagePath("images/pic.png"))
        assertEquals("./images/pic.png", gameImagePath("./images/pic.png"))
        assertEquals("images\\pic.png", gameImagePath("images\\pic.png"))
    }

    @Test
    fun `images from elsewhere are not opened`() {
        val sources = listOf("https://example.org/a.png", "data:image/png;base64,AAAA", "file:///sdcard/a.png", "", " ")
        for (src in sources) {
            assertNull(src, gameImagePath(src))
        }
    }
}
