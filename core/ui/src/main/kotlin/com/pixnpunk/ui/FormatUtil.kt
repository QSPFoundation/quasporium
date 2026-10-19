package com.pixnpunk.ui

import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import com.pixnpunk.dto.BinaryPrefFormat
import java.util.*
import kotlin.math.ln
import kotlin.math.pow

object FormatUtil {
    fun formatFileSize(size: Long, prefFormat: BinaryPrefFormat): String {
        if (size <= 0) return "0"

        val locale = Locale.getDefault()
        val numFormat = NumberFormat.getInstance(locale).apply {
            maximumFractionDigits = 1
        }
        val fmt = MeasureFormat.getInstance(
            locale,
            MeasureFormat.FormatWidth.SHORT,
            numFormat
        )

        val units = arrayOf(
            MeasureUnit.BYTE,
            MeasureUnit.KILOBYTE,
            MeasureUnit.MEGABYTE,
            MeasureUnit.GIGABYTE,
            MeasureUnit.TERABYTE
        )

        val unitSize = if (prefFormat == BinaryPrefFormat.SI) 1000.0 else 1024.0
        val digitGroups = (ln(size.toDouble()) / ln(unitSize)).toInt()
            .coerceAtMost(units.lastIndex)

        val value = size / unitSize.pow(digitGroups.toDouble())
        return fmt.format(Measure(value, units[digitGroups]))
    }
}
