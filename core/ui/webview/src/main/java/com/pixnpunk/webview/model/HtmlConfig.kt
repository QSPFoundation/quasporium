package com.pixnpunk.webview.model

import androidx.compose.runtime.Immutable
import com.pixnpunk.dto.FontTypeface
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Immutable
@Serializable
data class HtmlConfig(
    val typeface: FontTypeface = FontTypeface.DEFAULT,
    val fontSize: Int = 16,
    val textColor: Int = 0xFF000000.toInt(),
    val backColor: Int = 0xFFBDBDBD.toInt(),
    val linkColor: Int = 0xFF0000FF.toInt(),

    val isImgLoadDis: Boolean = false,
    val isAutoWidthImage: Boolean = true,
    val imageWidth: Int = 400,
    val isAutoHeightImage: Boolean = true,
    val imageHeight: Int = 400,
    val isUseFullscreenImages: Boolean = false,

    val isVideoMute: Boolean = true,

    @Transient
    val isAutoscroll: Boolean = true,
    @Transient
    val isUseHtml: Boolean = false
)