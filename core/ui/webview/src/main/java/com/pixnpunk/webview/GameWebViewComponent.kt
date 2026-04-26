package com.pixnpunk.webview

import com.pixnpunk.webview.mvi.GameWebViewStore
import kotlinx.coroutines.flow.StateFlow

interface GameWebViewComponent {
    val model: StateFlow<GameWebViewStore.State>

    val gameDirUriBox: String

    val onExecValue: (String) -> Unit
}