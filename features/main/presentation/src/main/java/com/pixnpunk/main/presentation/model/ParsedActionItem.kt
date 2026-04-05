package com.pixnpunk.main.presentation.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.pixnpunk.dto.LibGenItem

@Immutable
data class ParsedActionItem(
    val original: LibGenItem = LibGenItem(),
    val annotatedText: AnnotatedString = AnnotatedString(""),
    val hasImage: Boolean = false
)