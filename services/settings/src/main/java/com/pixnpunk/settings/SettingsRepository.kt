package com.pixnpunk.settings

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.pixnpunk.dto.AppThemeMode
import com.pixnpunk.dto.BinaryPrefFormat
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.FontTypeface
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.dto.PrefKeys
import com.pixnpunk.dto.SettingsEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class SettingsRepository : ViewModel(), KoinComponent {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(get())
    private val defValue = SettingsEntity()

    val currentSettings: SettingsEntity
        get() = readSettings()

    private val _settingsFlow = MutableStateFlow(readSettings())
    val settingsFlow: StateFlow<SettingsEntity> = _settingsFlow.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        _settingsFlow.value = readSettings()
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(null)
    }

    private fun readSettings() = SettingsEntity(
        fontSize = prefs.getInt(PrefKeys.FONT_SIZE, defValue.fontSize),

        isTextColorOverride = prefs.getBoolean(PrefKeys.TEXT_COLOR_OVERRIDE, defValue.isTextColorOverride),
        textColor = prefs.getInt(PrefKeys.TEXT_COLOR, defValue.textColor),

        isBackColorOverride = prefs.getBoolean(PrefKeys.BACK_COLOR_OVERRIDE, defValue.isBackColorOverride),
        backColor = prefs.getInt(PrefKeys.BACK_COLOR, defValue.backColor),

        isLinkColorOverride = prefs.getBoolean(PrefKeys.LINK_COLOR_OVERRIDE, defValue.isLinkColorOverride),
        linkColor = prefs.getInt(PrefKeys.LINK_COLOR, defValue.linkColor),

        isImgLoadDis = prefs.getBoolean(PrefKeys.IMG_LOAD_DISABLE, defValue.isImgLoadDis),
        isAutoWidthImage = prefs.getBoolean(PrefKeys.IMG_AUTO_WIDTH, defValue.isAutoWidthImage),
        imageWidth = prefs.getInt(PrefKeys.IMG_MANUAL_WIDTH, defValue.imageWidth),
        isAutoHeightImage = prefs.getBoolean(PrefKeys.IMG_AUTO_HEIGHT, defValue.isAutoHeightImage),
        imageHeight = prefs.getInt(PrefKeys.IMG_MANUAL_HEIGHT, defValue.imageHeight),
        isUseFullscreenImages = prefs.getBoolean(PrefKeys.IMG_IMMERSIVE, defValue.isUseFullscreenImages),
        isUseImageDebug = prefs.getBoolean(PrefKeys.IMG_DEBUG, defValue.isUseImageDebug),

        isSoundEnabled = prefs.getBoolean(PrefKeys.AUDIO_ENABLED, defValue.isSoundEnabled),
        isVideoMute = prefs.getBoolean(PrefKeys.VIDEO_MUTE, defValue.isVideoMute),
        isUseMusicDebug = prefs.getBoolean(PrefKeys.AUDIO_DEBUG, defValue.isUseMusicDebug),

        isUseExecString = prefs.getBoolean(PrefKeys.EXEC_STRING, defValue.isUseExecString),
        isImmersModeEnabled = prefs.getBoolean(PrefKeys.IMMERSIVE_MODE, defValue.isImmersModeEnabled),

        visActsCount = prefs.getInt(PrefKeys.ACTS_COUNT, defValue.visActsCount),
        language = prefs.getString(PrefKeys.APP_LANG, defValue.language) ?: defValue.language,

        typeface = runCatching {
            FontTypeface.valueOf(
                prefs.getString(PrefKeys.FONT_STYLE, defValue.typeface.name) ?: defValue.typeface.name
            )
        }.getOrDefault(defValue.typeface),

        nativeLibFrom = runCatching {
            NativeLibAuthors.valueOf(
                prefs.getString(PrefKeys.LIB_VERSION, defValue.nativeLibFrom.name) ?: defValue.nativeLibFrom.name
            )
        }.getOrDefault (defValue.nativeLibFrom),

        appThemeMode = runCatching {
            AppThemeMode.valueOf(
                prefs.getString("appThemePref", defValue.appThemeMode.name) ?: defValue.appThemeMode.name
            )
        }.getOrDefault(defValue.appThemeMode),

        binaryPref = runCatching {
            BinaryPrefFormat.valueOf(
                prefs.getString(PrefKeys.BINARY_PREF, defValue.binaryPref.name) ?: defValue.binaryPref.name
            )
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
        prefs.edit {
            if (state == ColorPickerState.COLOR_TEXT) putInt(PrefKeys.TEXT_COLOR, color.toArgb())
            if (state == ColorPickerState.COLOR_BACK) putInt(PrefKeys.BACK_COLOR, color.toArgb())
            if (state == ColorPickerState.COLOR_LINK) putInt(PrefKeys.LINK_COLOR, color.toArgb())
        }
    }

    fun updateImageSize(state: InputState, value: Int) {
        prefs.edit {
            if (state == InputState.INPUT_IMAGE_WIDTH) putInt(PrefKeys.IMG_MANUAL_WIDTH, value)
            if (state == InputState.INPUT_IMAGE_HEIGHT) putInt(PrefKeys.IMG_MANUAL_HEIGHT, value)
        }
    }
}
