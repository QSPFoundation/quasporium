package com.pixnpunk.utils

import androidx.compose.ui.text.font.FontFamily
import com.pixnpunk.dto.FontTypeface
import java.util.*
import java.util.concurrent.TimeUnit

object ViewUtil {
    fun FontTypeface.toComposeFamily(): FontFamily =
        when (this) {
            FontTypeface.DEFAULT -> FontFamily.Default
            FontTypeface.SANS_SERIF -> FontFamily.SansSerif
            FontTypeface.SERIF -> FontFamily.Serif
            FontTypeface.MONOSPACE -> FontFamily.Monospace
        }

    fun Long.formatEta(): String {
        if (this < 0) return "--:--"
        val hours = TimeUnit.MILLISECONDS.toHours(this)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}
