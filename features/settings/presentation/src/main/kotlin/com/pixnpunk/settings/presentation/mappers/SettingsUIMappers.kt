package com.pixnpunk.settings.presentation.mappers

import com.pixnpunk.dto.AppThemeMode
import com.pixnpunk.dto.BinaryPrefFormat
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.FontTypeface
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.settings.dto.model.PrefKeys
import com.pixnpunk.settings.dto.repository.SettingsRepository

fun SettingsEntity.toMap(): Map<String, Any> = mapOf(
    PrefKeys.FONT_STYLE to typeface.name,
    PrefKeys.FONT_SIZE to fontSize,
    PrefKeys.CUSTOM_FONT_ENABLED to isUseCustomFont,
    PrefKeys.CUSTOM_FONT_PATH to customFontPath,
    PrefKeys.TEXT_COLOR_OVERRIDE to isTextColorOverride,
    PrefKeys.TEXT_COLOR to textColor,
    PrefKeys.BACK_COLOR_OVERRIDE to isBackColorOverride,
    PrefKeys.BACK_COLOR to backColor,
    PrefKeys.LINK_COLOR_OVERRIDE to isLinkColorOverride,
    PrefKeys.LINK_COLOR to linkColor,
    PrefKeys.IMG_LOAD_DISABLE to isImgLoadDis,
    PrefKeys.IMG_AUTO_WIDTH to isAutoWidthImage,
    PrefKeys.IMG_MANUAL_WIDTH to imageWidth,
    PrefKeys.IMG_AUTO_HEIGHT to isAutoHeightImage,
    PrefKeys.IMG_MANUAL_HEIGHT to imageHeight,
    PrefKeys.IMG_IMMERSIVE to isUseFullscreenImages,
    PrefKeys.IMG_DEBUG to isUseImageDebug,
    PrefKeys.AUDIO_ENABLED to isSoundEnabled,
    PrefKeys.VIDEO_MUTE to isVideoMute,
    PrefKeys.AUDIO_DEBUG to isUseMusicDebug,
    PrefKeys.EXEC_STRING to isUseExecString,
    PrefKeys.IMMERSIVE_MODE to isImmersModeEnabled,
    PrefKeys.LIB_VERSION to nativeLibFrom.name,
    PrefKeys.ACTS_COUNT to visActsCount,
    PrefKeys.APP_LANG to language,
    PrefKeys.APP_THEME to appThemeMode.name,
    PrefKeys.BINARY_PREF to binaryPref.name,
)

suspend fun SettingsRepository.dispatch(key: String, value: Any) = when (key) {
    PrefKeys.FONT_STYLE -> updateFontStyle(FontTypeface.valueOf(value as String))
    PrefKeys.FONT_SIZE -> updateFontSize(value as Int)

    PrefKeys.CUSTOM_FONT_ENABLED -> toggleCustomFont(isEnabled = value as Boolean)
    PrefKeys.CUSTOM_FONT_PATH -> updateCustomFontPath(value as String)

    PrefKeys.TEXT_COLOR_OVERRIDE -> toggleColorOverride(ColorPickerState.COLOR_TEXT, value as Boolean)
    PrefKeys.TEXT_COLOR -> updateColorValue(ColorPickerState.COLOR_TEXT, value as Int)
    PrefKeys.BACK_COLOR_OVERRIDE -> toggleColorOverride(ColorPickerState.COLOR_BACK, value as Boolean)
    PrefKeys.BACK_COLOR -> updateColorValue(ColorPickerState.COLOR_BACK, value as Int)
    PrefKeys.LINK_COLOR_OVERRIDE -> toggleColorOverride(ColorPickerState.COLOR_LINK, value as Boolean)
    PrefKeys.LINK_COLOR -> updateColorValue(ColorPickerState.COLOR_LINK, value as Int)

    PrefKeys.IMG_LOAD_DISABLE -> toggleImageLoading(value as Boolean)
    PrefKeys.IMG_AUTO_WIDTH -> toggleAutoImageSize(InputState.INPUT_IMAGE_WIDTH, value as Boolean)
    PrefKeys.IMG_MANUAL_WIDTH -> updateImageSize(InputState.INPUT_IMAGE_WIDTH, value as Int)
    PrefKeys.IMG_AUTO_HEIGHT -> toggleAutoImageSize(InputState.INPUT_IMAGE_HEIGHT, value as Boolean)
    PrefKeys.IMG_MANUAL_HEIGHT -> updateImageSize(InputState.INPUT_IMAGE_HEIGHT, value as Int)
    PrefKeys.IMG_IMMERSIVE -> toggleFullscreenImages(value as Boolean)
    PrefKeys.IMG_DEBUG -> toggleImageDebug(value as Boolean)

    PrefKeys.AUDIO_ENABLED -> toggleSound(value as Boolean)
    PrefKeys.VIDEO_MUTE -> toggleVideoMute(value as Boolean)
    PrefKeys.AUDIO_DEBUG -> toggleAudioDebug(value as Boolean)

    PrefKeys.EXEC_STRING -> toggleExecString(value as Boolean)
    PrefKeys.IMMERSIVE_MODE -> toggleImmersiveMode(value as Boolean)
    PrefKeys.LIB_VERSION -> updateNativeLibVersion(NativeLibAuthors.valueOf(value as String))
    PrefKeys.ACTS_COUNT -> updateActsCount(value as Int)
    PrefKeys.APP_LANG -> updateLanguage(value as String)
    PrefKeys.APP_THEME -> updateTheme(AppThemeMode.valueOf(value as String))
    PrefKeys.BINARY_PREF -> updateBinaryPref(BinaryPrefFormat.valueOf(value as String))

    else -> {}
}
