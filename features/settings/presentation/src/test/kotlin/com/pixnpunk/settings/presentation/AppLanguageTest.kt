package com.pixnpunk.settings.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {

    @Test
    fun noAppLanguageFollowsTheSystem() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLanguageTags(""))
    }

    @Test
    fun regionalTagsMatchTheirLanguage() {
        assertEquals(AppLanguage.RUSSIAN, AppLanguage.fromLanguageTags("ru-RU"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLanguageTags("en-GB,ru"))
        assertEquals(AppLanguage.RUSSIAN, AppLanguage.fromLanguageTags("RU"))
    }

    @Test
    fun unsupportedLanguagesFollowTheSystem() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLanguageTags("de-DE"))
    }
}
