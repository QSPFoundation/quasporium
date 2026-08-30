package com.pixnpunk.dto

import kotlinx.serialization.Serializable

@Serializable
data class LibSaveItem(
    val fileName: String = "",
    val fileLastMod: Long = 0L,
    val fileUriBox: String = ""
)
