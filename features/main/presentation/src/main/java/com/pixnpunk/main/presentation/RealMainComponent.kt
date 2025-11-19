package com.pixnpunk.main.presentation

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.main.presentation.mvi.MainStore
import com.pixnpunk.main.presentation.mvi.RealMainStore
import com.pixnpunk.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RealMainComponent(
    private val componentContext: ComponentContext,
    private val gameWebViewClient: WebViewClient
) : ComponentContext by componentContext, MainComponent, KoinComponent {

    private val settingsRepo: SettingsRepository by inject()
    private val store = instanceKeeper.getStore {
        RealMainStore(
            storeFactory = DefaultStoreFactory(),
            stateKeeper = stateKeeper
        ).create()
    }

    init {
        stateKeeper.register(
            key = "MainStoreSavedState",
            strategy = MainStore.State.serializer(),
            supplier = { store.state }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<MainStore.State> = store.stateFlow
    override val settingsFlow = settingsRepo.settingsFlow
    override val onActionClicked: (Int) -> Unit = { index ->
        store.accept(MainStore.Intent.ActionClick(index))
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun getDefaultWebClient(view: WebView): WebView =
        view.apply {
            getSettings().allowFileAccess = true
            getSettings().javaScriptEnabled = true
            getSettings().useWideViewPort = true
            getSettings().blockNetworkLoads = true
            getSettings().cacheMode = WebSettings.LOAD_CACHE_ONLY
            setOverScrollMode(View.OVER_SCROLL_NEVER)
            setWebViewClient(gameWebViewClient)
        }
}
