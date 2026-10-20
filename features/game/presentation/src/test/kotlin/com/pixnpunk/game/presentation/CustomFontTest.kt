package com.pixnpunk.game.presentation

import com.pixnpunk.dto.SettingsEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CustomFontTest {

    private val loaded = mutableListOf<String>()
    private val load: (String) -> String? = { path ->
        loaded += path
        "font:$path"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun runWith(
        initial: SettingsEntity,
        block: (MutableStateFlow<SettingsEntity>) -> Unit = {}
    ): List<String?> {
        val results = mutableListOf<String?>()
        runTest(UnconfinedTestDispatcher()) {
            val settings = MutableStateFlow(initial)
            settings.customFont(load).onEach { results += it }.launchIn(backgroundScope)
            block(settings)
        }
        return results
    }

    @Test
    fun `no font when custom font is off`() {
        val results = runWith(SettingsEntity(isUseCustomFont = false, customFontPath = "/fonts/a.ttf"))

        assertEquals(listOf(null), results)
        assertEquals(emptyList<String>(), loaded)
    }

    @Test
    fun `no font when the path is empty`() {
        val results = runWith(SettingsEntity(isUseCustomFont = true, customFontPath = ""))

        assertEquals(listOf(null), results)
        assertEquals(emptyList<String>(), loaded)
    }

    @Test
    fun `custom font is loaded once`() {
        val results = runWith(SettingsEntity(isUseCustomFont = true, customFontPath = "/fonts/a.ttf"))

        assertEquals(listOf("font:/fonts/a.ttf"), results)
        assertEquals(listOf("/fonts/a.ttf"), loaded)
    }

    @Test
    fun `other settings changes do not reload the font`() {
        runWith(SettingsEntity(isUseCustomFont = true, customFontPath = "/fonts/a.ttf")) { settings ->
            settings.value = settings.value.copy(fontSize = settings.value.fontSize + 2)
        }

        assertEquals(listOf("/fonts/a.ttf"), loaded)
    }

    @Test
    fun `changing the path reloads the font`() {
        val results = runWith(SettingsEntity(isUseCustomFont = true, customFontPath = "/fonts/a.ttf")) { settings ->
            settings.value = settings.value.copy(customFontPath = "/fonts/b.ttf")
        }

        assertEquals(listOf("font:/fonts/a.ttf", "font:/fonts/b.ttf"), results)
        assertEquals(listOf("/fonts/a.ttf", "/fonts/b.ttf"), loaded)
    }
}
