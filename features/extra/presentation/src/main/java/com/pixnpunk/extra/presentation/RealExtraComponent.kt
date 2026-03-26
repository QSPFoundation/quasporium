package com.pixnpunk.extra.presentation

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.extra.presentation.mvi.ExtraStore
import com.pixnpunk.extra.presentation.mvi.RealExtraStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent

class RealExtraComponent(
    private val componentContext: ComponentContext,
    private val gameWebViewClient: WebViewClient,
    override val settingsState: StateFlow<SettingsEntity>
) : ComponentContext by componentContext, ExtraComponent, KoinComponent {

    private val store = instanceKeeper.getStore {
        RealExtraStore(
            storeFactory = DefaultStoreFactory(),
            stateKeeper = stateKeeper,
            settingsState = settingsState
        ).create()
    }

    init {
        stateKeeper.register(
            key = "ExtraStoreSavedState",
            strategy = ExtraStore.State.serializer(),
            supplier = { store.state }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<ExtraStore.State> = store.stateFlow

    @SuppressLint("SetJavaScriptEnabled")
    override fun getDefaultWebClient(view: WebView): WebView =
        view.apply {
            settings.allowFileAccess = false
            settings.javaScriptEnabled = true
            settings.blockNetworkLoads = true
            settings.domStorageEnabled = true
            if (settingsState.value.isVideoMute) {
                settings.mediaPlaybackRequiresUserGesture = false
            }
            setOverScrollMode(View.OVER_SCROLL_NEVER)
            setWebViewClient(gameWebViewClient)
        }
}
