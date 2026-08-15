package com.pixnpunk.main.presentation.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontFamily

@Immutable
data class MainUIState(
    val isActionsVis: Boolean = true,
    val visActsCount: Int = 3,
    val textColor: Int = 0xFF000000.toInt(),
    val fontSize: Int = 16,
    val backColor: Int = 0xFFBDBDBD.toInt(),
    val fontFamily: FontFamily = FontFamily.Default,
    val parsedActions: List<ParsedActionItem> = emptyList()
)
