package com.pixnpunk.settings.data.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.pixnpunk.settings.data.repository.SettingsRepositoryImpl
import com.pixnpunk.settings.dto.repository.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose

val settingsDataModule = module {
    single<SharedPreferences>(named("global_prefs")) {
        PreferenceManager.getDefaultSharedPreferences(androidContext())
    }

    factory<SettingsRepository> { params ->
        val gameId = params.getOrNull<Long>()?.takeIf { it != -1L }

        val globalSharedPrefs: SharedPreferences = get(named("global_prefs"))
        val localSharedPrefs: SharedPreferences? = if (gameId != null) {
            androidContext().getSharedPreferences(
                "game_settings_${gameId}",
                Context.MODE_PRIVATE
            )
        } else null

        SettingsRepositoryImpl(
            globalPrefs = globalSharedPrefs,
            localPrefs = localSharedPrefs
        )
    } onClose { it?.cancel() }
}
