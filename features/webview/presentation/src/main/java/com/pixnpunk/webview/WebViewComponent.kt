package com.pixnpunk.webview

import com.pixnpunk.webview.mvi.WebViewStore
import kotlinx.coroutines.flow.StateFlow

sealed interface WebViewComponent {
    val model: StateFlow<WebViewStore.State>

    fun onUserTouched()

    val gameDirUriBox: String

    val onExecValue: (String) -> Unit
}