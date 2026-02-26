package com.pixnpunk.extra.presentation

import android.webkit.WebView
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.extra.presentation.mvi.ExtraStore
import kotlinx.coroutines.flow.StateFlow

interface ExtraComponent {
    val model: StateFlow<ExtraStore.State>
    val settingsState: StateFlow<SettingsEntity>

    fun getDefaultWebClient(view: WebView): WebView
}