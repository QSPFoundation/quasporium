package com.pixnpunk.settings.data.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.pixnpunk.settings.data.repository.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val settingsDataModule = module {
    factory<SharedPreferences> { params ->
        val gameId = params.getOrNull<Long>()?.takeIf { it != -1L }
        if (gameId == null) {
            PreferenceManager.getDefaultSharedPreferences(androidContext())
        } else {
            androidContext().getSharedPreferences(
                "game_settings_${gameId}",
                Context.MODE_PRIVATE
            )
        }
    }

    factory<SettingsRepository> { params ->
        val gameId = params.getOrNull<Long>()?.takeIf { it != -1L }
        if (gameId == null) {
            SettingsRepository(
                globalPrefs = get(),
                localPrefs = null
            )
        } else {
            SettingsRepository(
                globalPrefs = get(),
                localPrefs = get { parametersOf(gameId) }
            )
        }
    }
}