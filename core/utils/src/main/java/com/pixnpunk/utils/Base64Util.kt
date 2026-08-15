package com.pixnpunk.utils

import android.util.Base64

object Base64Util {
    fun String.encodeBase64(flags: Int = Base64.DEFAULT): String {
        return Base64.encodeToString(this.toByteArray(), flags)
    }

    fun String.decodeBase64(): String {
        return String(Base64.decode(this, Base64.DEFAULT))
    }
}
