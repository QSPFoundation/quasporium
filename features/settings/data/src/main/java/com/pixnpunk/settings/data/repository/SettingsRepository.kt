package com.pixnpunk.settings.data.repository

import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.pixnpunk.dto.AppThemeMode
import com.pixnpunk.dto.BinaryPrefFormat
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.FontTypeface
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.settings.dto.model.PrefKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(
    private val globalPrefs: SharedPreferences,
    private val localPrefs: SharedPreferences? = null
) : InstanceKeeper.Instance {
    private val defValue = SettingsEntity()
    private val targetPrefsToEdit = localPrefs ?: globalPrefs

    private val _settingsFlow = MutableStateFlow(readSettings())
    val settingsFlow: StateFlow<SettingsEntity> = _settingsFlow.asStateFlow()
    val currentSettings: SettingsEntity get() = readSettings()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        _settingsFlow.value = readSettings()
    }

    init {
        globalPrefs.registerOnSharedPreferenceChangeListener(listener)
        localPrefs?.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onDestroy() {
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

    private fun readSettings() = SettingsEntity(
        fontSize = getIntMerged(PrefKeys.FONT_SIZE, defValue.fontSize),

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

        typeface = runCatching {
            FontTypeface.valueOf(getStringMerged(PrefKeys.FONT_STYLE, defValue.typeface.name))
        }.getOrDefault(defValue.typeface),

        nativeLibFrom = runCatching {
            NativeLibAuthors.valueOf(getStringMerged(PrefKeys.LIB_VERSION, defValue.nativeLibFrom.name))
        }.getOrDefault (defValue.nativeLibFrom),

        appThemeMode = runCatching {
            AppThemeMode.valueOf(getStringMerged(PrefKeys.APP_THEME, defValue.appThemeMode.name))
        }.getOrDefault(defValue.appThemeMode),

        binaryPref = runCatching {
            BinaryPrefFormat.valueOf(getStringMerged(PrefKeys.BINARY_PREF, defValue.binaryPref.name))
        }.getOrDefault(defValue.binaryPref),
    )

    fun emitValue(newSettings: SettingsEntity) {
        _settingsFlow.value = newSettings
    }

    fun restoreColorValue(state: ColorPickerState) {
        updateColorValue(
            state = state,
            color = Color(
                when (state) {
                    ColorPickerState.COLOR_TEXT -> defValue.textColor
                    ColorPickerState.COLOR_BACK -> defValue.backColor
                    ColorPickerState.COLOR_LINK -> defValue.linkColor
                }
            )
        )
    }

    fun updateColorValue(state: ColorPickerState, color: Color) {
        targetPrefsToEdit.edit {
            if (state == ColorPickerState.COLOR_TEXT) putInt(PrefKeys.TEXT_COLOR, color.toArgb())
            if (state == ColorPickerState.COLOR_BACK) putInt(PrefKeys.BACK_COLOR, color.toArgb())
            if (state == ColorPickerState.COLOR_LINK) putInt(PrefKeys.LINK_COLOR, color.toArgb())
        }
    }

    fun updateImageSize(state: InputState, value: Int) {
        targetPrefsToEdit.edit {
            if (state == InputState.INPUT_IMAGE_WIDTH) putInt(PrefKeys.IMG_MANUAL_WIDTH, value)
            if (state == InputState.INPUT_IMAGE_HEIGHT) putInt(PrefKeys.IMG_MANUAL_HEIGHT, value)
        }
    }
}