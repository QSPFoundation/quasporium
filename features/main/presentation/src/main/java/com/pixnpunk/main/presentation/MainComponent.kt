package com.pixnpunk.main.presentation

import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.main.presentation.mvi.MainStore
import com.pixnpunk.webview.WebViewComponent
import kotlinx.coroutines.flow.StateFlow

sealed interface MainComponent {
    val model: StateFlow<MainStore.State>
    val settingsState: StateFlow<SettingsEntity>

    val gameWebViewComponent: WebViewComponent

    val onActionClicked: (Int) -> Unit
}