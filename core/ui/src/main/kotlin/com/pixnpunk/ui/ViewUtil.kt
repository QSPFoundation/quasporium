package com.pixnpunk.ui

import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import com.pixnpunk.dto.FontTypeface
import java.io.File

object ViewUtil {
    fun FontTypeface.toComposeFamily(): FontFamily =
        when (this) {
            FontTypeface.DEFAULT -> FontFamily.Default
            FontTypeface.SANS_SERIF -> FontFamily.SansSerif
            FontTypeface.SERIF -> FontFamily.Serif
            FontTypeface.MONOSPACE -> FontFamily.Monospace
        }

    fun loadCustomFontFamily(path: String): FontFamily? =
        File(path).let {
            if (!it.exists()) return null
            runCatching {
                FontFamily(Typeface.createFromFile(it))
            }.getOrNull()
        }
}
