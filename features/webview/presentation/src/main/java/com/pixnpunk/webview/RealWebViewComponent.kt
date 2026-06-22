package com.pixnpunk.webview

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.webview.mvi.WebViewStore
import com.pixnpunk.webview.mvi.RealWebViewStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealWebViewComponent(
    private val componentContext: ComponentContext,
    private val htmlContentFlow: Flow<String>,
    private val settingsStateFlow: StateFlow<SettingsEntity>,
    override val gameDirUriBox: String,
    override val onExecValue: (String) -> Unit
) : ComponentContext by componentContext, WebViewComponent, KoinComponent {

    private val store = instanceKeeper.getStore {
        RealWebViewStore(
            storeFactory = DefaultStoreFactory(),
            htmlContentFlow = htmlContentFlow,
            settingsStateFlow = settingsStateFlow,
            formatGameHtmlUseCase = get()
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<WebViewStore.State> = store.stateFlow

    override fun onUserTouched() =
        store.accept(WebViewStore.Intent.OnUserTouchedWebView)
}