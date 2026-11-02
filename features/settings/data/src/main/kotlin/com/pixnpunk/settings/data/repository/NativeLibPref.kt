package com.pixnpunk.settings.data.repository

import com.pixnpunk.dto.NativeLib

internal fun nativeLibFromPref(value: String, default: NativeLib): NativeLib =
    when (value) {
        "BYTE" -> NativeLib.QSP
        "SEEDHARTHA" -> NativeLib.QSPLEGACY
        else -> NativeLib.entries.find { it.name == value } ?: default
    }
