package com.pixnpunk.utils

object ColorUtil {
    fun Int.toHexColorStr(): String {
        return String.format("#%06X", 0xFFFFFF and this)
    }
}
