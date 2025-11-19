package com.pixnpunk.utils

object ColorUtil {
    fun Int.convertRGBAtoBGRA(): Int {
        return -0x1000000 or
                ((this and 0x000000ff) shl 16) or
                (this and 0x0000ff00) or
                ((this and 0x00ff0000) shr 16)
    }

    fun Int.toHexColorStr(): String {
        return String.format("#%06X", 0xFFFFFF and this)
    }
}