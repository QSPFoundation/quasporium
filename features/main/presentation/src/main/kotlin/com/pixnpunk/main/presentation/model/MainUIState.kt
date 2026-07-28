package com.pixnpunk.main.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class MainUIState(
    val isActionsVis: Boolean = true,
    val textColor: Int = 0xFF000000.toInt(),
    val fontSize: Int = 16,
    val backColor: Int = 0xFFBDBDBD.toInt(),
    val parsedActions: List<ParsedActionItem> = emptyList()
)
