package com.pixnpunk.main.presentation

import androidx.compose.ui.text.font.FontFamily
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.main.presentation.model.MainUIState
import com.pixnpunk.main.presentation.mvi.MainStore
import com.pixnpunk.webview.presentation.WebViewComponent
import kotlinx.coroutines.flow.StateFlow

sealed interface MainComponent {
    val model: StateFlow<MainStore.State>
    val settingsState: StateFlow<SettingsEntity>
    val uiState: StateFlow<MainUIState>
    val fontFamilyState: StateFlow<FontFamily>

    val gameWebViewComponent: WebViewComponent

    val onActionClicked: (Int) -> Unit
}
