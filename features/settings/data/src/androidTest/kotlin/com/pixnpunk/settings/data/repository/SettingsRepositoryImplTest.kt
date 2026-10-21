package com.pixnpunk.settings.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.settings.domain.model.PrefKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.milliseconds

@RunWith(AndroidJUnit4::class)
class SettingsRepositoryImplTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val globalPrefs = context.getSharedPreferences("settings_test_global", Context.MODE_PRIVATE)
    private val localPrefs = context.getSharedPreferences("settings_test_local", Context.MODE_PRIVATE)
    private val repositories = mutableListOf<SettingsRepositoryImpl>()

    @Before
    @After
    fun clearPrefs() {
        repositories.forEach { it.cancel() }
        repositories.clear()
        globalPrefs.edit(commit = true) { clear() }
        localPrefs.edit(commit = true) { clear() }
    }

    @Test
    fun gameWritesGoToTheGameFileOnly() = runBlocking {
        val game = repository(localPrefs)

        game.updateFontSize(30)
        game.toggleSound(false)

        assertEquals(30, localPrefs.getInt(PrefKeys.FONT_SIZE, 0))
        assertEquals(false, localPrefs.getBoolean(PrefKeys.AUDIO_ENABLED, true))
        assertFalse(globalPrefs.contains(PrefKeys.FONT_SIZE))
        assertFalse(globalPrefs.contains(PrefKeys.AUDIO_ENABLED))
        game.awaitSettings { it.fontSize == 30 && !it.isSoundEnabled }
    }

    @Test
    fun globalWritesGoToTheGlobalFile() = runBlocking {
        val global = repository(null)

        global.updateFontSize(22)

        assertEquals(22, globalPrefs.getInt(PrefKeys.FONT_SIZE, 0))
        assertFalse(localPrefs.contains(PrefKeys.FONT_SIZE))
    }

    @Test
    fun gameInheritsGlobalValuesItDoesNotOverride() = runBlocking {
        globalPrefs.edit(commit = true) {
            putInt(PrefKeys.FONT_SIZE, 20)
            putBoolean(PrefKeys.IMMERSIVE_MODE, false)
        }
        localPrefs.edit(commit = true) { putInt(PrefKeys.FONT_SIZE, 30) }

        val settings = repository(localPrefs).settingsFlow.value

        assertEquals(30, settings.fontSize)
        assertEquals(false, settings.isImmersModeEnabled)
    }

    @Test
    fun globalChangesReachAnOpenGameUnlessOverridden() = runBlocking {
        localPrefs.edit(commit = true) { putInt(PrefKeys.ACTS_COUNT, 5) }
        val game = repository(localPrefs)
        val global = repository(null)

        global.updateFontSize(25)
        global.updateActsCount(7)

        game.awaitSettings { it.fontSize == 25 }
        assertEquals(5, game.settingsFlow.value.visActsCount)
    }

    @Test
    fun restoringAColorWritesTheDefaultIntoTheGameFile() = runBlocking {
        globalPrefs.edit(commit = true) { putInt(PrefKeys.TEXT_COLOR, 0x112233) }
        val game = repository(localPrefs)
        game.updateColorValue(ColorPickerState.COLOR_TEXT, 0x445566)

        game.restoreColorValue(ColorPickerState.COLOR_TEXT)

        assertEquals(SettingsEntity().textColor, localPrefs.getInt(PrefKeys.TEXT_COLOR, 0))
        assertEquals(0x112233, globalPrefs.getInt(PrefKeys.TEXT_COLOR, 0))
    }

    private fun repository(local: SharedPreferences?) =
        SettingsRepositoryImpl(globalPrefs, local).also { repositories += it }

    private suspend fun SettingsRepositoryImpl.awaitSettings(predicate: (SettingsEntity) -> Boolean) {
        withTimeout(5_000.milliseconds) { settingsFlow.first(predicate) }
    }
}
