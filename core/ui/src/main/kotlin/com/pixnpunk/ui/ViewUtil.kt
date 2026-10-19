package com.pixnpunk.ui

import androidx.compose.ui.text.font.FontFamily
import com.pixnpunk.dto.FontTypeface

object ViewUtil {
    fun FontTypeface.toComposeFamily(): FontFamily =
        when (this) {
            FontTypeface.DEFAULT -> FontFamily.Default
            FontTypeface.SANS_SERIF -> FontFamily.SansSerif
            FontTypeface.SERIF -> FontFamily.Serif
            FontTypeface.MONOSPACE -> FontFamily.Monospace
        }
}
