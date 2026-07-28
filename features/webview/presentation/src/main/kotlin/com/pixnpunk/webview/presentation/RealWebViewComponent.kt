package com.pixnpunk.webview.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.domain.model.LibUIConfig
import com.pixnpunk.webview.presentation.mvi.RealWebViewStore
import com.pixnpunk.webview.presentation.mvi.WebViewStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealWebViewComponent(
    private val componentContext: ComponentContext,
    private val htmlContentFlow: Flow<String>,
    private val settingsStateFlow: StateFlow<SettingsEntity>,
    private val engineSettingsStateFlow: StateFlow<LibUIConfig>,
    override val gameDirUriBox: String,
    override val onExecValue: (String) -> Unit
) : ComponentContext by componentContext, WebViewComponent, KoinComponent {

    private val scope = coroutineScope()

    private val modSettingsFlow: StateFlow<SettingsEntity> =
        settingsStateFlow
            .combine(engineSettingsStateFlow) { appSet, libSet ->
                appSet.copy(
                    isUseHtml = libSet.isUseHtml,
                    textColor = libSet.fontColor.takeIf { !appSet.isTextColorOverride && it != 0 } ?: appSet.textColor,
                    backColor = libSet.backColor.takeIf { !appSet.isBackColorOverride && it != 0 } ?: appSet.backColor,
                    linkColor = libSet.linkColor.takeIf { !appSet.isLinkColorOverride && it != 0 } ?: appSet.linkColor,
                    fontSize = libSet.fontSize.takeIf { appSet.isUseGameFont && it != 0 } ?: appSet.fontSize
                )
            }.stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = settingsStateFlow.value
            )

    private val store = instanceKeeper.getStore {
        RealWebViewStore(
            storeFactory = DefaultStoreFactory(),
            htmlContentFlow = htmlContentFlow,
            settingsStateFlow = modSettingsFlow,
            formatGameHtmlUseCase = get()
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<WebViewStore.State> = store.stateFlow

    override fun onUserTouched() =
        store.accept(WebViewStore.Intent.OnUserTouchedWebView)
}
