package com.pixnpunk.stock.data.mapper

import com.pixnpunk.stock.data.remote.RemoteGameData
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteGameMapperTest {

    private fun game(iconUrl: String?, coverUrl: String?) = RemoteGameData(
        id = 305,
        name = "Game",
        ver = "1",
        authors = "Author",
        descriptionHtml = "",
        iconUrl = iconUrl,
        coverUrl = coverUrl,
        fileUrl = "https://qsp.org/storage/games/305/game.zip",
        downloadsCount = 0,
        playsCount = 0,
        likesCount = 0,
        commentsCount = 0,
        createdAt = "",
        updatedAt = ""
    )

    private fun imageOf(iconUrl: String?, coverUrl: String?) =
        listOf(game(iconUrl, coverUrl)).toDomain().single().gameIconPath

    @Test
    fun `the cover is shown when the game has one`() {
        assertEquals(
            "https://qsp.org/storage/games/305/cover.jpg",
            imageOf(
                iconUrl = "https://qsp.org/storage/games/305/icon.jpg",
                coverUrl = "https://qsp.org/storage/games/305/cover.jpg"
            )
        )
    }

    @Test
    fun `the small icon is the fallback`() {
        val icon = "https://qsp.org/storage/games/305/icon.jpg"

        assertEquals(icon, imageOf(icon, null))
        assertEquals(icon, imageOf(icon, " "))
    }

    @Test
    fun `a game without images has none`() {
        assertEquals("", imageOf(null, null))
    }
}
