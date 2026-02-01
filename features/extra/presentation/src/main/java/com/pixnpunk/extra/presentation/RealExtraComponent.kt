package com.pixnpunk.extra.presentation

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.extra.presentation.mvi.ExtraStore
import com.pixnpunk.extra.presentation.mvi.RealExtraStore
import com.pixnpunk.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RealExtraComponent(
    private val componentContext: ComponentContext,
    private val gameWebViewClient: WebViewClient,
) : ComponentContext by componentContext, ExtraComponent, KoinComponent {

    private val settingsRepo: SettingsRepository by inject()
    private val store = instanceKeeper.getStore {
        RealExtraStore(DefaultStoreFactory()).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<ExtraStore.State> = store.stateFlow
    override val settingsFlow = settingsRepo.settingsFlow

    @SuppressLint("SetJavaScriptEnabled")
    override fun getDefaultWebClient(view: WebView): WebView =
        view.apply {
            settings.allowFileAccess = false
            settings.javaScriptEnabled = true
            settings.blockNetworkLoads = true
            settings.domStorageEnabled = true
            if (settingsFlow.value.isVideoMute) {
                settings.mediaPlaybackRequiresUserGesture = false
            }
            setOverScrollMode(View.OVER_SCROLL_NEVER)
            setWebViewClient(gameWebViewClient)
        }
}
