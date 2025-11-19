package com.pixnpunk.main.presentation

import android.webkit.WebView
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.main.presentation.mvi.MainStore
import kotlinx.coroutines.flow.StateFlow

sealed interface MainComponent {
    val model: StateFlow<MainStore.State>
    val settingsFlow: StateFlow<SettingsEntity>

    val onActionClicked: (Int) -> Unit
    fun getDefaultWebClient(view: WebView): WebView
}