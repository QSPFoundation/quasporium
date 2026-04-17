package com.pixnpunk.extra.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.extra.presentation.mvi.ExtraStore.Message.UpdateExtraDesc
import com.pixnpunk.game.data.GameNativeEngine
import com.pixnpunk.utils.HtmlUtil.appendPageTemplate
import com.pixnpunk.utils.HtmlUtil.getCleanHtmlAndMedia
import com.pixnpunk.utils.HtmlUtil.getCleanHtmlRemMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ExtraExecutor(
    private val gameNativeEngine: GameNativeEngine,
    private val settingsState: StateFlow<SettingsEntity>,
) : CoroutineExecutor<ExtraStore.Intent, ExtraStore.Action, ExtraStore.State, ExtraStore.Message, ExtraStore.Label>() {

    override fun executeAction(action: ExtraStore.Action) {
        when (action) {
            is ExtraStore.Action.StartExtraReceiveFlow -> {
                scope.launch(Dispatchers.IO) {
                    gameNativeEngine.gameStateFlow
                        .combine(settingsState) { state, settings -> state to settings }
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
}