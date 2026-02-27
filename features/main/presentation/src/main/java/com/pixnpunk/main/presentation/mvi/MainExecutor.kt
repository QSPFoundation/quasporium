package com.pixnpunk.main.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.dto.LibTypeWindow
import com.pixnpunk.main.presentation.mvi.MainStore.Message.UpdateActions
import com.pixnpunk.main.presentation.mvi.MainStore.Message.UpdateMainDesc
import com.pixnpunk.main.presentation.mvi.MainStore.Message.UpdateVisActions
import com.pixnpunk.natives.NativeViewModel
import com.pixnpunk.settings.SettingsRepository
import com.pixnpunk.utils.HtmlUtil.appendPageTemplate
import com.pixnpunk.utils.HtmlUtil.getCleanHtmlAndMedia
import com.pixnpunk.utils.HtmlUtil.getCleanHtmlRemMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class MainExecutor(
    private val service: NativeViewModel,
    private val settingsRepo: SettingsRepository
) : CoroutineExecutor<MainStore.Intent, MainStore.Action, MainStore.State, MainStore.Message, MainStore.Label>() {
    private val outScope = CoroutineScope(Dispatchers.Default)

    override fun executeIntent(intent: MainStore.Intent) {
        when (intent) {
            is MainStore.Intent.ActionClick -> service.onActionClicked(intent.index)
        }
    }

    override fun executeAction(action: MainStore.Action) {
        when (action) {
            is MainStore.Action.StartActionsVisFlow -> {
                outScope.launch {
                    service.gameElementVis
                        .filter { it.first == LibTypeWindow.ACTS }
                        .collect { scope.launch { dispatch(UpdateVisActions(it.second)) } }
                }
            }

            is MainStore.Action.StartContentFlow -> {
                outScope.launch {
                    service.gameStateFlow
                        .combine(settingsRepo.settingsFlow) { state, settings -> state to settings }
                        .collect { (state, settings) ->
                            scope.launch {
                                dispatch(
                                    UpdateMainDesc(
                                        appendPageTemplate(
                                            settings,
                                            state.mainDesc
                                        ).let { s ->
                                            if (settings.isImgLoadDis) {
                                                s.getCleanHtmlRemMedia()
                                            } else {
                                                s.getCleanHtmlAndMedia(settings)
                                            }
                                        }
                                    )
                                )
                                dispatch(UpdateActions(state.actionsList))
                            }
                        }
                }
            }
        }
    }

    override fun dispose() {
        super.dispose()
        outScope.cancel()
    }
}