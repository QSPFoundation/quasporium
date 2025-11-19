package com.pixnpunk.utils

import android.util.Base64
import java.util.regex.Pattern

object Base64Util {
    private const val BASE64_PATTERN =
        "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$"
    private val pattern: Pattern = Pattern.compile(BASE64_PATTERN)

    fun String.encodeBase64(flags: Int = Base64.DEFAULT): String {
        return Base64.encodeToString(this.toByteArray(), flags)
    }

    fun String.decodeBase64(): String {
        return String(Base64.decode(this, Base64.DEFAULT))
    }

    fun String.isBase64(): Boolean {
        return pattern.matcher(this).find()
    }
}