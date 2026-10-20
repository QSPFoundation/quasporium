package com.pixnpunk.game.presentation

import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.domain.repository.AudioPlayerInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SoundSettingTest {

    private val calls = mutableListOf<String>()

    private val audioPlayer = object : AudioPlayerInteractor {
        override var isSoundEnabled = true
        override fun isPlayingFile(path: String) = false
        override fun playFile(path: String, volume: Int) = Unit
        override fun closeFile(path: String) = Unit
        override fun closeAllFiles() = Unit
        override fun pauseAll() {
            calls += "pause"
        }

        override fun resumeAll() {
            calls += "resume"
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `disabled sound is applied to the player on start`() = runTest(UnconfinedTestDispatcher()) {
        val settings = MutableStateFlow(SettingsEntity(isSoundEnabled = false))

        settings.applySoundSetting(audioPlayer).launchIn(backgroundScope)

        assertFalse(audioPlayer.isSoundEnabled)
        assertEquals(listOf("pause"), calls)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `toggling sound pauses and resumes playback`() = runTest(UnconfinedTestDispatcher()) {
        val settings = MutableStateFlow(SettingsEntity(isSoundEnabled = true))
        settings.applySoundSetting(audioPlayer).launchIn(backgroundScope)

        settings.value = settings.value.copy(isSoundEnabled = false)
        settings.value = settings.value.copy(isSoundEnabled = true)

        assertEquals(listOf("resume", "pause", "resume"), calls)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `other settings changes do not touch playback`() = runTest(UnconfinedTestDispatcher()) {
        val settings = MutableStateFlow(SettingsEntity(isSoundEnabled = true))
        settings.applySoundSetting(audioPlayer).launchIn(backgroundScope)

        settings.value = settings.value.copy(fontSize = settings.value.fontSize + 2)

        assertEquals(listOf("resume"), calls)
    }
}
