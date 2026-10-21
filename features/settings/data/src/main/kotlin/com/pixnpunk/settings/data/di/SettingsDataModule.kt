package com.pixnpunk.settings.data.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.pixnpunk.settings.data.repository.GameSettingsPreloaderImpl
import com.pixnpunk.settings.data.repository.SettingsRepositoryImpl
import com.pixnpunk.settings.data.repository.gameSettingsName
import com.pixnpunk.settings.domain.repository.GameSettingsPreloader
import com.pixnpunk.settings.domain.repository.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose

private val globalPrefsQualifier = named("global_prefs")

val settingsDataModule = module {
    single<SharedPreferences>(globalPrefsQualifier) {
        PreferenceManager.getDefaultSharedPreferences(androidContext()).also { it.all }
    }

    factory<SettingsRepository> { params ->
        val gameId = params.getOrNull<Long>()?.takeIf { it != -1L }

        val globalSharedPrefs: SharedPreferences = get(globalPrefsQualifier)
        val localSharedPrefs: SharedPreferences? =
            if (gameId == null) {
                null
            } else {
                androidContext().getSharedPreferences(
                    gameSettingsName(gameId),
                    Context.MODE_PRIVATE
                )
            }

        SettingsRepositoryImpl(
            globalPrefs = globalSharedPrefs,
            localPrefs = localSharedPrefs
        )
    } onClose { it?.cancel() }

    single<GameSettingsPreloader> {
        GameSettingsPreloaderImpl(
            androidContext(),
            inject<SharedPreferences>(globalPrefsQualifier)
        )
    }
}

fun Koin.preloadGlobalSettings() {
    get<SharedPreferences>(globalPrefsQualifier)
}
