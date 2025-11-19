package com.pixnpunk.dto

import kotlinx.serialization.Serializable

@Serializable
data class LibUIConfig(
    val useHtml: Boolean = false,
    val fontSize: Int = 0,
    val backColor: Int = 0,
    val fontColor: Int = 0,
    val linkColor: Int = 0
)