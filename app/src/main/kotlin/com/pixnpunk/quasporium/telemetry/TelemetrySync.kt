package com.pixnpunk.quasporium.telemetry

import com.arkivanov.decompose.router.slot.child
import com.pixnpunk.dto.OutputState
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.logger.Telemetry
import com.pixnpunk.quasporium.RealRootComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class TelemetrySync(
    private val telemetry: Telemetry,
    private val settings: Flow<SettingsEntity>
) {
    fun start(scope: CoroutineScope): Job? {
        if (!telemetry.isAvailable) return null

        return settings
            .map { it.isUsageStatsEnabled to it.isCrashReportsEnabled }
            .distinctUntilChanged()
            .onEach { (usageStats, crashReports) ->
                telemetry.setUsageStatisticsEnabled(usageStats)
                telemetry.setCrashReportsEnabled(crashReports)
            }
            .launchIn(scope)
    }
}

fun shouldAskTelemetryConsent(
    telemetry: Telemetry,
    settings: SettingsEntity
): Boolean = telemetry.isAvailable && !settings.isTelemetryConsentAsked

fun RealRootComponent.askTelemetryConsentIfNeeded(
    telemetry: Telemetry,
    message: String
) {
    if (!shouldAskTelemetryConsent(telemetry, settingsState.value)) return
    if (dialogSlot.child != null) return

    doShowDialog(outputValue = message, outputState = OutputState.OUTPUT_CONSENT)
}
