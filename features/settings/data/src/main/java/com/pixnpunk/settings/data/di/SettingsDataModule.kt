package com.pixnpunk.settings.data.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.pixnpunk.settings.data.repository.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val settingsDataModule = module {
    single<SharedPreferences>(named("GlobalPrefs")) {
        PreferenceManager.getDefaultSharedPreferences(androidContext())
    }

    factory<SharedPreferences>(named("LocalPrefs")) {
        androidContext().getSharedPreferences(
            "game_settings_${it.get<Long>()}",
            Context.MODE_PRIVATE
        )
    }

    single<SettingsRepository>(named("GlobalRepository")) {
        SettingsRepository(
            globalPrefs = get(named("GlobalPrefs")),
            localPrefs = null
        )
    }

    factory<SettingsRepository>(named("GameRepository")) {
        SettingsRepository(
            globalPrefs = get(named("GlobalPrefs")),
            localPrefs = get(named("LocalPrefs")) { parametersOf(it.get<Long>()) }
        )
    }
}