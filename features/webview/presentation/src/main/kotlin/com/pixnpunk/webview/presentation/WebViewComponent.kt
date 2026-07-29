package com.pixnpunk.webview.presentation

import com.pixnpunk.webview.presentation.model.WebViewState
import kotlinx.coroutines.flow.StateFlow

sealed interface WebViewComponent {
    fun onUserTouched()
    val model: StateFlow<WebViewState>
    val gameDirUriBox: String
    val onExecValue: (String) -> Unit
}
