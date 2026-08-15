package com.pixnpunk.main.presentation

import com.pixnpunk.main.presentation.model.MainUIState
import com.pixnpunk.webview.presentation.WebViewComponent
import kotlinx.coroutines.flow.StateFlow

sealed interface MainComponent {
    val uiState: StateFlow<MainUIState>

    val gameWebViewComponent: WebViewComponent

    val onActionClicked: (Int) -> Unit
}
