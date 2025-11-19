package com.pixnpunk.utils

import android.graphics.Typeface
import com.pixnpunk.dto.FontTypeface

object ViewUtil {
    fun FontTypeface.toTypeface(): Typeface =
        when (this) {
            FontTypeface.DEFAULT -> Typeface.DEFAULT
            FontTypeface.SANS_SERIF -> Typeface.SANS_SERIF
            FontTypeface.SERIF -> Typeface.SERIF
            FontTypeface.MONOSPACE -> Typeface.MONOSPACE
        }
}