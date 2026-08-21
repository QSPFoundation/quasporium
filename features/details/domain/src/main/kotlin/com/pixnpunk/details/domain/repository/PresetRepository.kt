package com.pixnpunk.details.domain.repository

import com.pixnpunk.details.domain.model.PresetItem
import kotlinx.coroutines.flow.Flow

interface PresetRepository {
    fun observeAllPresets(gameId: Long): Flow<List<PresetItem>>
    suspend fun getAllPresets(gameId: Long): List<PresetItem>
    suspend fun createPreset(gameId: Long, presetName: String, filePath: String? = null): Long
    suspend fun deletePreset(presetId: Long)
    suspend fun updateTimestamp(presetId: Long)
    suspend fun updateLastPlayedFile(presetId: Long, fileName: String)
    suspend fun updateVisuals(presetId: Long, name: String, accentColor: Long)
    suspend fun saveBackgroundImage(presetId: Long, sourceUriBox: String)
    suspend fun removeBackgroundImage(presetId: Long)
}
