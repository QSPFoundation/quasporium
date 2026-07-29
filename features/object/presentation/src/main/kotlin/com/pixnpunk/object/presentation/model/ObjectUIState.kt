package com.pixnpunk.`object`.presentation.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontFamily

@Immutable
data class ObjectUIState(
    val textColor: Int = 0xFF000000.toInt(),
    val fontSize: Int = 16,
    val backColor: Int = 0xFFBDBDBD.toInt(),
    val parsedObjects: List<ParsedObjectItem> = emptyList(),
    val fontFamily: FontFamily = FontFamily.Default
)
