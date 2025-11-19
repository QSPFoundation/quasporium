package com.pixnpunk.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixnpunk.dto.BinaryPrefFormat
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.FontTypeface
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.dto.SettingsEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsRepository : ViewModel() {
    private val _settingsFlow: MutableStateFlow<SettingsEntity> = MutableStateFlow(SettingsEntity())
    val settingsFlow: StateFlow<SettingsEntity> = _settingsFlow

    fun emitValue(settings: SettingsEntity) {
        viewModelScope.launch {
            _settingsFlow.emit(settings)
        }
    }

    fun emitColorValue(state: ColorPickerState, color: Color) {
        viewModelScope.launch {
            _settingsFlow.emit(
                when (state) {
                    ColorPickerState.COLOR_TEXT -> SettingsEntity(textColor = color.toArgb())
                    ColorPickerState.COLOR_BACK -> SettingsEntity(backColor = color.toArgb())
                    ColorPickerState.COLOR_LINK -> SettingsEntity(linkColor = color.toArgb())
                }
            )
        }
    }

    fun emitImageConf(state: InputState, config: String) {
        viewModelScope.launch {
            _settingsFlow.emit(
                if (state == InputState.INPUT_IMAGE_WIDTH) {
                    SettingsEntity(imageWidth = config.toInt())
                } else {
                    SettingsEntity(imageHeight = config.toInt())
                }
            )
        }
    }

    fun emitMap(prefMap: Map<String, Any>) {
        if (prefMap.entries.isNotEmpty()) {
            val defValue = settingsFlow.value
            viewModelScope.launch {
                _settingsFlow.emit(
                    SettingsEntity(
                        typeface = FontTypeface.valueOf(
                            prefMap["fontStylePref"] as? String ?: defValue.typeface.name
                        ),
                        fontSize = prefMap["fontSizePref"] as? Int ?: defValue.fontSize,

                        isUseGameTextColor = prefMap["useGameTextColorPref"] as? Boolean
                            ?: defValue.isUseGameTextColor,
                        textColor = prefMap["textColor"] as? Int ?: defValue.textColor,
                        isUseGameBackgroundColor = prefMap["useGameBackColorPref"] as? Boolean
                            ?: defValue.isUseGameBackgroundColor,
                        backColor = prefMap["backColor"] as? Int ?: defValue.backColor,
                        isUseGameLinkColor = prefMap["useGameLinkColorPref"] as? Boolean
                            ?: defValue.isUseGameLinkColor,
                        linkColor = prefMap["linkColor"] as? Int ?: defValue.linkColor,

                        isImgLoadDis = prefMap["useGameImagesPref"] as? Boolean
                            ?: defValue.isImgLoadDis,
                        isAutoWidthImage = prefMap["useAutoImageWidthPref"] as? Boolean
                            ?: defValue.isAutoWidthImage,
                        imageWidth = prefMap["manualImageWidthPref"] as? Int
                            ?: defValue.imageWidth,
                        isAutoHeightImage = prefMap["useAutoImageHeightPref"] as? Boolean
                            ?: defValue.isAutoHeightImage,
                        imageHeight = prefMap["manualImageHeightPref"] as? Int
                            ?: defValue.imageHeight,
                        isUseFullscreenImages = prefMap["useImmersImagesPref"] as? Boolean
                            ?: defValue.isUseFullscreenImages,
                        isUseImageDebug = prefMap["useGameImageDebug"] as? Boolean
                            ?: defValue.isUseImageDebug,

                        isSoundEnabled = prefMap["useGameAudioPref"] as? Boolean
                            ?: defValue.isSoundEnabled,
                        isVideoMute = prefMap["useMuteGameVideoPref"] as? Boolean
                            ?: defValue.isVideoMute,
                        isUseMusicDebug = prefMap["useGameAudioDebug"] as? Boolean
                            ?: defValue.isUseMusicDebug,

                        isUseExecString = prefMap["useGameExecStringPref"] as? Boolean
                            ?: defValue.isUseExecString,
                        isImmersModeEnabled = prefMap["appImmersiveModePref"] as? Boolean
                            ?: defValue.isImmersModeEnabled,
                        nativeLibFrom = NativeLibAuthors.valueOf(
                            prefMap["appLibVerPref"] as? String
                                ?: defValue.nativeLibFrom.name
                        ),
                        visActsCount = prefMap["countActsPref"] as? Int
                            ?: defValue.visActsCount,
                        language = prefMap["appLangPref"] as? String ?: defValue.language,
                        theme = prefMap["appThemePref"] as? String ?: defValue.theme,

                        binaryPref = BinaryPrefFormat.valueOf(
                            prefMap["appBinPref"] as? String
                                ?: defValue.binaryPref.name
                        )
                    )
                )
            }
        }
    }
}
