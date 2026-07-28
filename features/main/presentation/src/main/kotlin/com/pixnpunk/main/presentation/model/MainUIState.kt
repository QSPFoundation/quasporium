package com.pixnpunk.main.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class MainUIState(
    val isActionsVis: Boolean = true,
    val parsedActions: List<ParsedActionItem> = emptyList()
)
