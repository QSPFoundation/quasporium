package com.pixnpunk.utils

import android.graphics.Typeface
import com.pixnpunk.dto.FontTypeface
import java.util.*
import java.util.concurrent.TimeUnit

object ViewUtil {
    fun FontTypeface.toTypeface(): Typeface =
        when (this) {
            FontTypeface.DEFAULT -> Typeface.DEFAULT
            FontTypeface.SANS_SERIF -> Typeface.SANS_SERIF
            FontTypeface.SERIF -> Typeface.SERIF
            FontTypeface.MONOSPACE -> Typeface.MONOSPACE
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