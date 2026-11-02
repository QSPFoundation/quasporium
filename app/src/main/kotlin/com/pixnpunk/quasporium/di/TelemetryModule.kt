package com.pixnpunk.quasporium.di

import com.pixnpunk.quasporium.telemetry.TelemetrySync
import com.pixnpunk.settings.domain.repository.SettingsRepository
import org.koin.dsl.module

val telemetryModule = module {
    single { TelemetrySync(get(), get<SettingsRepository>().settingsFlow) }
}
