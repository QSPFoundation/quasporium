package com.pixnpunk.quasporium.logger

import com.pixnpunk.logger.GameStartEvent
import com.pixnpunk.logger.Telemetry

internal class NoTelemetry : Telemetry {
    override val isAvailable = false

    override fun setUsageStatisticsEnabled(enabled: Boolean) = Unit

    override fun setCrashReportsEnabled(enabled: Boolean) = Unit

    override fun logGameStarted(event: GameStartEvent) = Unit
}
