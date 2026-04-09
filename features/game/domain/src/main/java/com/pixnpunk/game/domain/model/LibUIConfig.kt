package com.pixnpunk.game.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LibUIConfig(
    val isYesScroll: Boolean = true,
    val isNoSave: Boolean = false,
    val isUseHtml: Boolean = false,
    val fontSize: Int = 0,
    val backColor: Int = 0,
    val fontColor: Int = 0,
    val linkColor: Int = 0
)