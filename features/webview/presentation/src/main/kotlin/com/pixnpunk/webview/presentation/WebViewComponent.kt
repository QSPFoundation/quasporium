package com.pixnpunk.webview.presentation

import com.pixnpunk.webview.presentation.mvi.WebViewStore
import kotlinx.coroutines.flow.StateFlow

sealed interface WebViewComponent {
    val model: StateFlow<WebViewStore.State>

    fun onUserTouched()

    val gameDirUriBox: String

    val onExecValue: (String) -> Unit
}
