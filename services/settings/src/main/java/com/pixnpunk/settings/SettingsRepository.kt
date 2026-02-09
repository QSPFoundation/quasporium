package com.pixnpunk.settings

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.SettingsEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import androidx.core.content.edit
import com.pixnpunk.dto.AppThemeMode
import com.pixnpunk.dto.BinaryPrefFormat
import com.pixnpunk.dto.FontTypeface
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.dto.PrefKeys

class SettingsRepository : ViewModel(), KoinComponent {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(get())
    private val defValue = SettingsEntity()

    val currentSettings: SettingsEntity
        get() = readSettings()

    val settingsFlow: Flow<SettingsEntity> = callbackFlow {
        trySend(readSettings())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(readSettings())
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
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
        prefs.edit {
            putString(PrefKeys.FONT_STYLE, newSettings.typeface.name)
            putInt(PrefKeys.FONT_SIZE, newSettings.fontSize)

            putBoolean(PrefKeys.TEXT_COLOR_OVERRIDE, newSettings.isTextColorOverride)
            putInt(PrefKeys.TEXT_COLOR, newSettings.textColor)
            putBoolean(PrefKeys.BACK_COLOR_OVERRIDE, newSettings.isBackColorOverride)
            putInt(PrefKeys.BACK_COLOR, newSettings.backColor)
            putBoolean(PrefKeys.LINK_COLOR_OVERRIDE, newSettings.isLinkColorOverride)
            putInt(PrefKeys.LINK_COLOR, newSettings.linkColor)

            putBoolean(PrefKeys.IMG_LOAD_DISABLE, newSettings.isImgLoadDis)
            putBoolean(PrefKeys.IMG_AUTO_WIDTH, newSettings.isAutoWidthImage)
            putInt(PrefKeys.IMG_MANUAL_WIDTH, newSettings.imageWidth)
            putBoolean(PrefKeys.IMG_AUTO_HEIGHT, newSettings.isAutoHeightImage)
            putInt(PrefKeys.IMG_MANUAL_HEIGHT, newSettings.imageHeight)
            putBoolean(PrefKeys.IMG_IMMERSIVE, newSettings.isUseFullscreenImages)
            putBoolean(PrefKeys.IMG_DEBUG, newSettings.isUseImageDebug)

            putBoolean(PrefKeys.AUDIO_ENABLED, newSettings.isSoundEnabled)
            putBoolean(PrefKeys.VIDEO_MUTE, newSettings.isVideoMute)
            putBoolean(PrefKeys.AUDIO_DEBUG, newSettings.isUseMusicDebug)

            putBoolean(PrefKeys.EXEC_STRING, newSettings.isUseExecString)
            putBoolean(PrefKeys.IMMERSIVE_MODE, newSettings.isImmersModeEnabled)
            putString(PrefKeys.LIB_VERSION, newSettings.nativeLibFrom.name)
            putInt(PrefKeys.ACTS_COUNT, newSettings.visActsCount)
            putString(PrefKeys.APP_LANG, newSettings.language)
            putString(PrefKeys.APP_THEME, newSettings.appThemeMode.name)
            putString(PrefKeys.BINARY_PREF, newSettings.binaryPref.name)
        }
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
