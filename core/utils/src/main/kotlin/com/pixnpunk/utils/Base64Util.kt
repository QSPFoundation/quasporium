package com.pixnpunk.utils

import kotlin.io.encoding.Base64

object Base64Util {
    fun String.encodeBase64(base64: Base64 = Base64.Default): String =
        base64.encode(this.encodeToByteArray())

    fun String.decodeBase64(base64: Base64 = Base64.Default): String =
        base64.decode(this).decodeToString()
}
