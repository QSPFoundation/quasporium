package com.pixnpunk.quasporium.telemetry

import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.logger.GameStartEvent
import com.pixnpunk.logger.Telemetry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TelemetrySyncTest {

    private class RecordingTelemetry(override val isAvailable: Boolean = true) : Telemetry {
        val calls = mutableListOf<String>()

        override fun setUsageStatisticsEnabled(enabled: Boolean) {
            calls += "usage=$enabled"
        }

        override fun setCrashReportsEnabled(enabled: Boolean) {
            calls += "crashes=$enabled"
        }

        override fun logGameStarted(event: GameStartEvent) {
            calls += "game"
        }
    }

    private val telemetry = RecordingTelemetry()
    private val settings = MutableStateFlow(SettingsEntity())

    @Test
    fun appliesTheStoredChoicesOnStart() = runTest(UnconfinedTestDispatcher()) {
        TelemetrySync(telemetry, settings).start(backgroundScope)

        assertEquals(listOf("usage=false", "crashes=true"), telemetry.calls)
    }

    @Test
    fun followsTheSwitches() = runTest(UnconfinedTestDispatcher()) {
        TelemetrySync(telemetry, settings).start(backgroundScope)
        telemetry.calls.clear()

        settings.value = settings.value.copy(isUsageStatsEnabled = true)
        settings.value = settings.value.copy(isCrashReportsEnabled = false)

        assertEquals(
            listOf("usage=true", "crashes=true", "usage=true", "crashes=false"),
            telemetry.calls
        )
    }

    @Test
    fun ignoresOtherSettings() = runTest(UnconfinedTestDispatcher()) {
        TelemetrySync(telemetry, settings).start(backgroundScope)
        telemetry.calls.clear()

        settings.value = settings.value.copy(fontSize = 20, isTelemetryConsentAsked = true)

        assertEquals(emptyList<String>(), telemetry.calls)
    }

    @Test
    fun doesNothingWithoutTelemetry() = runTest(UnconfinedTestDispatcher()) {
        val noTelemetry = RecordingTelemetry(isAvailable = false)

        assertNull(TelemetrySync(noTelemetry, settings).start(backgroundScope))
        assertEquals(emptyList<String>(), noTelemetry.calls)
    }

    @Test
    fun asksForConsentOnceAndOnlyWithTelemetry() {
        assertTrue(shouldAskTelemetryConsent(telemetry, SettingsEntity()))
        assertFalse(shouldAskTelemetryConsent(telemetry, SettingsEntity(isTelemetryConsentAsked = true)))
        assertFalse(shouldAskTelemetryConsent(RecordingTelemetry(isAvailable = false), SettingsEntity()))
    }
}
