package com.pixnpunk.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SettingsEntity(
    val typeface: FontTypeface = FontTypeface.DEFAULT,
    val fontSize: Int = 16,
    val isUseGameFont: Boolean = false,
    val isUseGameTextColor: Boolean = true,
    val textColor: Int = 0xFF000000.toInt(),
    val isUseGameBackgroundColor: Boolean = true,
    val backColor: Int = 0xFFBDBDBD.toInt(),
    val isUseGameLinkColor: Boolean = true,
    val linkColor: Int = 0xFF0000FF.toInt(),

    val isImgLoadDis: Boolean = false,
    val isAutoWidthImage: Boolean = true,
    val imageWidth: Int = 400,
    val isAutoHeightImage: Boolean = true,
    val imageHeight: Int = 400,
    val isUseFullscreenImages: Boolean = false,
    val isUseImageDebug: Boolean = false,

    val isSoundEnabled: Boolean = true,
    val isVideoMute: Boolean = true,
    val isUseMusicDebug: Boolean = false,

    val isUseExecString: Boolean = false,
    val isImmersModeEnabled: Boolean = true,
    val nativeLibFrom: NativeLibAuthors = NativeLibAuthors.SEEDHARTHA,
    val visActsCount: Int = 3,
    val language: String = "",
    val theme: String = "",

    @Transient
    val binaryPref: BinaryPrefFormat = BinaryPrefFormat.SI,
    @Transient
    val isUseHtml: Boolean = true
)
