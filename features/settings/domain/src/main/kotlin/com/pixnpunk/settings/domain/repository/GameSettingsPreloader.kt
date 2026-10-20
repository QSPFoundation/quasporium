package com.pixnpunk.settings.domain.repository

interface GameSettingsPreloader {
    suspend fun preloadGameSettings(gameId: Long)
}
