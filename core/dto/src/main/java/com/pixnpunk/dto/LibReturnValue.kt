package com.pixnpunk.dto

import kotlinx.serialization.Serializable

@Serializable
data class LibReturnValue(
    val dialogTextValue: String = "",
    val dialogNumValue: Int = 0,
    val playFileState: Boolean = false,
    val filePath: String = ""
)