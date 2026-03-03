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
import com.pixnpunk.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealExtraComponent(
    private val componentContext: ComponentContext,
    private val gameWebViewClient: WebViewClient,
) : ComponentContext by componentContext, ExtraComponent, KoinComponent {

    private val store = instanceKeeper.getStore {
        RealExtraStore(
            storeFactory = DefaultStoreFactory(),
            stateKeeper = stateKeeper
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
    override val settingsState: StateFlow<SettingsEntity> =
        get<SettingsRepository>().settingsFlow.stateIn(
            scope = CoroutineScope(Dispatchers.Main),
            started = SharingStarted.Eagerly,
            initialValue = get<SettingsRepository>().currentSettings
        )

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
