package com.pixnpunk.extra.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.extra.presentation.mvi.ExtraStore.Message.UpdateExtraDesc
import com.pixnpunk.natives.SupervisorViewModel
import com.pixnpunk.settings.SettingsRepository
import com.pixnpunk.utils.HtmlUtil.appendPageTemplate
import com.pixnpunk.utils.HtmlUtil.getCleanHtmlAndMedia
import com.pixnpunk.utils.HtmlUtil.getCleanHtmlRemMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ExtraExecutor(
    private val service: SupervisorViewModel,
    private val settingsRepo: SettingsRepository
) : CoroutineExecutor<ExtraStore.Intent, ExtraStore.Action, ExtraStore.State, ExtraStore.Message, ExtraStore.Label>() {
    private val outScope = CoroutineScope(Dispatchers.Default)

    override fun executeAction(action: ExtraStore.Action) {
        when (action) {
            is ExtraStore.Action.StartExtraReceiveFlow -> {
                outScope.launch {
                    service.gameStateFlow
                        .combine(settingsRepo.settingsFlow) { state, settings -> state to settings }
                        .collect { (state, settings) ->
                            scope.launch {
                                dispatch(
                                    UpdateExtraDesc(
                                        appendPageTemplate(
                                            settings,
                                            state.varsDesc
                                        ).let { s ->
                                            if (settings.isImgLoadDis) {
                                                s.getCleanHtmlRemMedia()
                                            } else {
                                                s.getCleanHtmlAndMedia(settings)
                                            }
                                        }
                                    )
                                )
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