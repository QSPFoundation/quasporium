package com.pixnpunk.main.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun MainContentActionsListHolder(
    component: MainComponent
) {
    val state by component.model.collectAsState()
    val settings by component.settingsFlow.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(settings.backColor)
    ) {
        if (state.isActionsVis && state.actions.isNotEmpty()) {
            MainContentActionsList(
                countActsVis = settings.visActsCount,
                settings = settings,
                actions = state.actions,
                onActionClicked = component.onActionClicked,
                listState = rememberLazyListState()
            )
        }
    }
}