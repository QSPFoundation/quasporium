package com.pixnpunk.settings.dto.repository

import com.pixnpunk.dto.AppThemeMode
import com.pixnpunk.dto.BinaryPrefFormat
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.FontTypeface
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.dto.SettingsEntity
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settingsFlow: StateFlow<SettingsEntity>

    fun cancel()

    suspend fun updateFontSize(fontSize: Int)
    suspend fun updateFontStyle(typeface: FontTypeface)

    suspend fun toggleCustomFont(isEnabled: Boolean)
    suspend fun updateCustomFontPath(filePath: String)

    suspend fun toggleColorOverride(state: ColorPickerState, isOverride: Boolean)
    suspend fun updateColorValue(state: ColorPickerState, colorArgb: Int)
    suspend fun restoreColorValue(state: ColorPickerState)

    suspend fun toggleImageLoading(isDisabled: Boolean)
    suspend fun toggleAutoImageSize(state: InputState, isAuto: Boolean)
    suspend fun updateImageSize(state: InputState, value: Int)
    suspend fun toggleFullscreenImages(isImmersive: Boolean)
    suspend fun toggleImageDebug(isEnabled: Boolean)

    suspend fun toggleSound(isEnabled: Boolean)
    suspend fun toggleVideoMute(isMuted: Boolean)
    suspend fun toggleAudioDebug(isEnabled: Boolean)

    suspend fun updateLanguage(langCode: String)
    suspend fun updateTheme(theme: AppThemeMode)
    suspend fun updateBinaryPref(format: BinaryPrefFormat)
    suspend fun updateNativeLibVersion(version: NativeLibAuthors)
    suspend fun updateActsCount(count: Int)
    suspend fun toggleImmersiveMode(isEnabled: Boolean)
    suspend fun toggleExecString(isEnabled: Boolean)
}
