package com.pixnpunk.settings.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.pixnpunk.settings.domain.repository.GameSettingsPreloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal fun gameSettingsName(gameId: Long) = "game_settings_$gameId"

internal class GameSettingsPreloaderImpl(
    private val context: Context,
    private val globalPrefs: Lazy<SharedPreferences>
) : GameSettingsPreloader {
    override suspend fun preloadGameSettings(gameId: Long) {
        withContext(Dispatchers.IO) {
            globalPrefs.value
            context.getSharedPreferences(gameSettingsName(gameId), Context.MODE_PRIVATE).all
        }
    }
}
