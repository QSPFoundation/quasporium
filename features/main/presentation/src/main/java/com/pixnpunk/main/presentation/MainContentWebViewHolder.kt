package com.pixnpunk.main.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun MainContentWebViewHolder(
    component: MainComponent
) {
    val state by component.model.collectAsState()
    val settings by component.settingsFlow.collectAsState()

    MainContentWebView(
        mainDesc = state.mainDesc,
        backColor = settings.backColor,
        paddingValues = PaddingValues(),
        setupWebClient = component::getDefaultWebClient
    )
}