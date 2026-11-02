package com.pixnpunk.logger

interface Telemetry {
    val isAvailable: Boolean

    fun setUsageStatisticsEnabled(enabled: Boolean)

    fun setCrashReportsEnabled(enabled: Boolean)

    fun logGameStarted(event: GameStartEvent)
}

data class GameStartEvent(
    val engine: String,
    val isCatalogGame: Boolean,
    val title: String,
    val isContinue: Boolean
)
