package com.pixnpunk.details.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class CatalogDateTest {

    @Test
    fun parsesIsoDateWithOffset() {
        assertEquals(Instant.parse("2024-03-01T09:30:00Z"), parseCatalogDate("2024-03-01T12:30:00+03:00"))
        assertEquals(Instant.parse("2024-03-01T12:30:00.123Z"), parseCatalogDate("2024-03-01T12:30:00.123Z"))
    }

    @Test
    fun parsesLocalDateAsUtc() {
        assertEquals(Instant.parse("2019-11-05T08:00:00Z"), parseCatalogDate("2019-11-05 08:00:00"))
    }

    @Test
    fun returnsNullForUnknownFormat() {
        assertNull(parseCatalogDate(""))
        assertNull(parseCatalogDate("05.11.2019"))
    }
}
