package com.pixnpunk.settings.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.pixnpunk.dto.AppThemeMode
import com.pixnpunk.dto.BinaryPrefFormat
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.FontTypeface
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.settings.domain.model.PrefKeys
import com.pixnpunk.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepositoryImpl(
    private val globalPrefs: SharedPreferences,
    private val localPrefs: SharedPreferences? = null,
) : SettingsRepository {

    private val defValue = SettingsEntity()
    private val targetPrefs = localPrefs ?: globalPrefs

    private val _settingsFlow = MutableStateFlow(readSettings())
    override val settingsFlow: StateFlow<SettingsEntity> = _settingsFlow.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        _settingsFlow.value = readSettings()
    }

    init {
        globalPrefs.registerOnSharedPreferenceChangeListener(listener)
        localPrefs?.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun cancel() {
        globalPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        localPrefs?.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun getIntMerged(key: String, default: Int): Int =
        if (localPrefs != null && localPrefs.contains(key)) {
            localPrefs.getInt(key, default)
        } else {
            globalPrefs.getInt(key, default)
        }

    private fun getBooleanMerged(key: String, default: Boolean): Boolean =
        if (localPrefs != null && localPrefs.contains(key)) {
            localPrefs.getBoolean(key, default)
        } else {
            globalPrefs.getBoolean(key, default)
        }

    private fun getStringMerged(key: String, default: String): String =
        if (localPrefs != null && localPrefs.contains(key)) {
            localPrefs.getString(key, default) ?: default
        } else {
            globalPrefs.getString(key, default) ?: default
        }

    private inline fun <reified T : Enum<T>> parseEnumSafety(value: String, default: T): T =
        enumValues<T>().find { it.name == value } ?: default

    private fun readSettings() = SettingsEntity(
        fontSize = getIntMerged(PrefKeys.FONT_SIZE, defValue.fontSize),

        isUseCustomFont = getBooleanMerged(PrefKeys.CUSTOM_FONT_ENABLED, defValue.isUseCustomFont),
        customFontPath = getStringMerged(PrefKeys.CUSTOM_FONT_PATH, defValue.customFontPath),

        isTextColorOverride = getBooleanMerged(PrefKeys.TEXT_COLOR_OVERRIDE, defValue.isTextColorOverride),
        textColor = getIntMerged(PrefKeys.TEXT_COLOR, defValue.textColor),

        isBackColorOverride = getBooleanMerged(PrefKeys.BACK_COLOR_OVERRIDE, defValue.isBackColorOverride),
        backColor = getIntMerged(PrefKeys.BACK_COLOR, defValue.backColor),

        isLinkColorOverride = getBooleanMerged(PrefKeys.LINK_COLOR_OVERRIDE, defValue.isLinkColorOverride),
        linkColor = getIntMerged(PrefKeys.LINK_COLOR, defValue.linkColor),

        isImgLoadDis = getBooleanMerged(PrefKeys.IMG_LOAD_DISABLE, defValue.isImgLoadDis),
        isAutoWidthImage = getBooleanMerged(PrefKeys.IMG_AUTO_WIDTH, defValue.isAutoWidthImage),
        imageWidth = getIntMerged(PrefKeys.IMG_MANUAL_WIDTH, defValue.imageWidth),
        isAutoHeightImage = getBooleanMerged(PrefKeys.IMG_AUTO_HEIGHT, defValue.isAutoHeightImage),
        imageHeight = getIntMerged(PrefKeys.IMG_MANUAL_HEIGHT, defValue.imageHeight),
        isUseFullscreenImages = getBooleanMerged(PrefKeys.IMG_IMMERSIVE, defValue.isUseFullscreenImages),
        isUseImageDebug = getBooleanMerged(PrefKeys.IMG_DEBUG, defValue.isUseImageDebug),

        isSoundEnabled = getBooleanMerged(PrefKeys.AUDIO_ENABLED, defValue.isSoundEnabled),
        isVideoMute = getBooleanMerged(PrefKeys.VIDEO_MUTE, defValue.isVideoMute),
        isUseMusicDebug = getBooleanMerged(PrefKeys.AUDIO_DEBUG, defValue.isUseMusicDebug),

        isUseExecString = getBooleanMerged(PrefKeys.EXEC_STRING, defValue.isUseExecString),
        isImmersModeEnabled = getBooleanMerged(PrefKeys.IMMERSIVE_MODE, defValue.isImmersModeEnabled),

        visActsCount = getIntMerged(PrefKeys.ACTS_COUNT, defValue.visActsCount),
        language = getStringMerged(PrefKeys.APP_LANG, defValue.language),

        typeface = parseEnumSafety(
            getStringMerged(PrefKeys.FONT_STYLE, defValue.typeface.name),
            defValue.typeface
        ),

        nativeLibFrom = parseEnumSafety(
            getStringMerged(PrefKeys.LIB_VERSION, defValue.nativeLibFrom.name),
            defValue.nativeLibFrom
        ),

        appThemeMode = parseEnumSafety(
            getStringMerged(PrefKeys.APP_THEME, defValue.appThemeMode.name),
            defValue.appThemeMode
        ),

        binaryPref = parseEnumSafety(
            getStringMerged(PrefKeys.BINARY_PREF, defValue.binaryPref.name),
            defValue.binaryPref
        ),
    )

    override suspend fun updateFontSize(fontSize: Int) =
        targetPrefs.edit { putInt(PrefKeys.FONT_SIZE, fontSize) }

    override suspend fun updateFontStyle(typeface: FontTypeface) =
        targetPrefs.edit { putString(PrefKeys.FONT_STYLE, typeface.name) }

    override suspend fun toggleCustomFont(isEnabled: Boolean) =
        targetPrefs.edit { putBoolean(PrefKeys.CUSTOM_FONT_ENABLED, isEnabled) }

    override suspend fun updateCustomFontPath(filePath: String) =
        targetPrefs.edit { putString(PrefKeys.CUSTOM_FONT_PATH, filePath) }

    override suspend fun toggleColorOverride(state: ColorPickerState, isOverride: Boolean) =
        targetPrefs.edit {
            when (state) {
                ColorPickerState.COLOR_TEXT -> putBoolean(PrefKeys.TEXT_COLOR_OVERRIDE, isOverride)
                ColorPickerState.COLOR_BACK -> putBoolean(PrefKeys.BACK_COLOR_OVERRIDE, isOverride)
                ColorPickerState.COLOR_LINK -> putBoolean(PrefKeys.LINK_COLOR_OVERRIDE, isOverride)
            }
        }

    override suspend fun updateColorValue(state: ColorPickerState, colorArgb: Int) =
        targetPrefs.edit {
            when (state) {
                ColorPickerState.COLOR_TEXT -> putInt(PrefKeys.TEXT_COLOR, colorArgb)
                ColorPickerState.COLOR_BACK -> putInt(PrefKeys.BACK_COLOR, colorArgb)
                ColorPickerState.COLOR_LINK -> putInt(PrefKeys.LINK_COLOR, colorArgb)
            }
        }

    override suspend fun restoreColorValue(state: ColorPickerState) =
        updateColorValue(
            state,
            when (state) {
                ColorPickerState.COLOR_TEXT -> defValue.textColor
                ColorPickerState.COLOR_BACK -> defValue.backColor
                ColorPickerState.COLOR_LINK -> defValue.linkColor
            }
        )

    override suspend fun toggleImageLoading(isDisabled: Boolean) =
        targetPrefs.edit { putBoolean(PrefKeys.IMG_LOAD_DISABLE, isDisabled) }

    override suspend fun toggleAutoImageSize(state: InputState, isAuto: Boolean) =
        targetPrefs.edit {
            when (state) {
                InputState.INPUT_IMAGE_WIDTH -> putBoolean(PrefKeys.IMG_AUTO_WIDTH, isAuto)
                InputState.INPUT_IMAGE_HEIGHT -> putBoolean(PrefKeys.IMG_AUTO_HEIGHT, isAuto)
                else -> {}
            }
        }

    override suspend fun updateImageSize(state: InputState, value: Int) =
        targetPrefs.edit {
            when (state) {
                InputState.INPUT_IMAGE_WIDTH -> putInt(PrefKeys.IMG_MANUAL_WIDTH, value)
                InputState.INPUT_IMAGE_HEIGHT -> putInt(PrefKeys.IMG_MANUAL_HEIGHT, value)
                else -> {}
            }
        }

    override suspend fun toggleFullscreenImages(isImmersive: Boolean) =
        targetPrefs.edit { putBoolean(PrefKeys.IMG_IMMERSIVE, isImmersive) }

    override suspend fun toggleImageDebug(isEnabled: Boolean) =
        targetPrefs.edit { putBoolean(PrefKeys.IMG_DEBUG, isEnabled) }

    override suspend fun toggleSound(isEnabled: Boolean) =
        targetPrefs.edit { putBoolean(PrefKeys.AUDIO_ENABLED, isEnabled) }

    override suspend fun toggleVideoMute(isMuted: Boolean) =
        targetPrefs.edit { putBoolean(PrefKeys.VIDEO_MUTE, isMuted) }

    override suspend fun toggleAudioDebug(isEnabled: Boolean) =
        targetPrefs.edit { putBoolean(PrefKeys.AUDIO_DEBUG, isEnabled) }

    override suspend fun updateLanguage(langCode: String) =
        targetPrefs.edit { putString(PrefKeys.APP_LANG, langCode) }

    override suspend fun updateTheme(theme: AppThemeMode) =
        targetPrefs.edit { putString(PrefKeys.APP_THEME, theme.name) }

    override suspend fun updateBinaryPref(format: BinaryPrefFormat) =
        targetPrefs.edit { putString(PrefKeys.BINARY_PREF, format.name) }

    override suspend fun updateNativeLibVersion(version: NativeLibAuthors) =
        targetPrefs.edit { putString(PrefKeys.LIB_VERSION, version.name) }

    override suspend fun updateActsCount(count: Int) =
        targetPrefs.edit { putInt(PrefKeys.ACTS_COUNT, count) }

    override suspend fun toggleImmersiveMode(isEnabled: Boolean) =
        targetPrefs.edit { putBoolean(PrefKeys.IMMERSIVE_MODE, isEnabled) }

    override suspend fun toggleExecString(isEnabled: Boolean) =
        targetPrefs.edit { putBoolean(PrefKeys.EXEC_STRING, isEnabled) }
}
