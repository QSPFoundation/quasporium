package com.pixnpunk.details.presentation.mapper

import com.pixnpunk.dto.GameEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.ocpsoft.prettytime.PrettyTime
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

class GameEntryDisplayMapperTest {

    private val prettyTime = PrettyTime(Locale.ENGLISH)

    @Test
    fun catalogueDatesBecomeRelativeTime() {
        val now = Instant.now()
        val entry = GameEntry(
            pubDate = now.minus(3, ChronoUnit.DAYS).toString(),
            modDate = now.minus(2, ChronoUnit.HOURS).toString()
        )

        val shown = entry.withRelativeDates(prettyTime)

        assertEquals("3 days ago", shown.pubDate)
        assertEquals("2 hours ago", shown.modDate)
    }

    @Test
    fun unknownOrEmptyDatesStayAsTheyAre() {
        val entry = GameEntry(pubDate = "05.11.2019", modDate = "")

        assertEquals(entry, entry.withRelativeDates(prettyTime))
    }
}
