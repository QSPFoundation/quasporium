package com.pixnpunk.main.presentation.model

data class MainUIState(
    val isActionsVis: Boolean = true,
    val parsedActions: List<ParsedActionItem> = emptyList()
)
