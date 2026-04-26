package com.pixnpunk.main.presentation

import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.main.presentation.mvi.MainStore
import com.pixnpunk.webview.GameWebViewComponent
import kotlinx.coroutines.flow.StateFlow

sealed interface MainComponent {
    val model: StateFlow<MainStore.State>
    val settingsState: StateFlow<SettingsEntity>

    val gameWebViewComponent: GameWebViewComponent

    val onActionClicked: (Int) -> Unit
}