package com.pixnpunk.game.presentation.mvi

import android.content.Context
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.LibReturnValue
import com.pixnpunk.dto.LibTypeDialog
import com.pixnpunk.dto.LibTypePopup
import com.pixnpunk.dto.LibTypeWindow
import com.pixnpunk.game.presentation.mvi.GameStore.Label.ShowDialog
import com.pixnpunk.game.presentation.mvi.GameStore.Label.ShowLoadFilePicker
import com.pixnpunk.game.presentation.mvi.GameStore.Label.ShowSaveFilePicker
import com.pixnpunk.natives.NativeViewModel
import com.pixnpunk.settings.SettingsRepository
import com.pixnpunk.utils.PathUtil.getImageUriFromPath
import com.pixnpunk.utils.PathUtil.normalizeContentPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch

internal class GameExecutor(
    private val appContext: Context,
    private val service: NativeViewModel,
    private val settingsRepo: SettingsRepository
) : CoroutineExecutor<GameStore.Intent, GameStore.Action, GameStore.State, GameStore.Message, GameStore.Label>() {

    override fun executeIntent(intent: GameStore.Intent) {
        when (intent) {
            is GameStore.Intent.OnLoadFile ->
                if (state().isUserChooseSave) {
                    dispatch(GameStore.Message.UpdateIsUserChooseSave(false))
                    service.onLoadFile(intent.fileUri)
                } else {
                    service.putReturnValue(
                        LibReturnValue(
                            filePath = intent.fileUri.toString()
                        )
                    )
                }

            is GameStore.Intent.OnSaveFile ->
                if (state().isUserChooseSave) {
                    dispatch(GameStore.Message.UpdateIsUserChooseSave(false))
                    service.onSaveFile(intent.fileUri)
                } else {
                    service.putReturnValue(
                        LibReturnValue(
                            filePath = intent.fileUri.toString()
                        )
                    )
                }

            is GameStore.Intent.OnCorruptFile -> service.putReturnValue(null)

            is GameStore.Intent.OnExecCode -> scope.launch { service.onCodeExec(intent.codeToExec) }

            is GameStore.Intent.CleanMainSavedDesc ->
                dispatch(
                    GameStore.Message.UpdateIsMainDescUpdated(
                        state().isMainDescUpdated.copy(second = false)
                    )
                )

            is GameStore.Intent.CleanExtraSavedDesc ->
                dispatch(
                    GameStore.Message.UpdateIsExtraDescUpdated(
                        state().isExtraDescUpdated.copy(second = false)
                    )
                )


            is GameStore.Intent.CleanObjectsSavedList ->
                dispatch(
                    GameStore.Message.UpdateIsObjsListUpdated(
                        state().isObjectsListUpdated.copy(second = false)
                    )
                )

            is GameStore.Intent.OnSelectMenuItem ->
                service.putReturnValue(
                    LibReturnValue(dialogNumValue = intent.index)
                )

            is GameStore.Intent.StartLoadFile -> {
                if (!state().isGameDirInternal) {
                    dispatch(GameStore.Message.UpdateIsUserChooseSave(true))
                    publish(ShowLoadFilePicker)
                } else {

                }
            }

            is GameStore.Intent.StartSaveFile -> {
                if (!state().isGameDirInternal) {
                    dispatch(GameStore.Message.UpdateIsUserChooseSave(true))
                    publish(ShowSaveFilePicker)
                } else {

                }
            }

            is GameStore.Intent.OnInputValue ->
                when (intent.state) {
                    InputState.INPUT_WITH_MESSAGE_AREA ->
                        service.onUseInputArea(intent.inputString)

                    InputState.INPUT_WITH_MESSAGE_BOX ->
                        service.putReturnValue(
                            LibReturnValue(dialogTextValue = intent.inputString)
                        )

                    else -> service.onCodeExec(intent.inputString)
                }

            is GameStore.Intent.OnDismissDialog ->
                service.putReturnValue(LibReturnValue())
        }
    }

    override fun executeAction(action: GameStore.Action) {
        when (action) {
            is GameStore.Action.StartGameStateFlow ->
                scope.launch(Dispatchers.IO) {
                    service.gameStateFlow.collect { state ->
                        scope.launch {
                            dispatch(GameStore.Message.UpdateGameStatus(state.gameRunning))

                            if (state.mainDesc.isNotBlank()) {
                                if (state().isMainDescUpdated.first != state.mainDesc.hashCode()) {
                                    dispatch(
                                        GameStore.Message.UpdateIsMainDescUpdated(
                                            state.mainDesc.hashCode() to true
                                        )
                                    )
                                }
                            }

                            if (state.varsDesc.isNotBlank()) {
                                if (state().isExtraDescUpdated.first != state.varsDesc.hashCode()) {
                                    dispatch(
                                        GameStore.Message.UpdateIsExtraDescUpdated(
                                            state.varsDesc.hashCode() to true
                                        )
                                    )
                                }
                            }

                            if (state.objectsList.isNotEmpty()) {
                                if (state().isObjectsListUpdated.first != state.objectsList.hashCode()) {
                                    dispatch(
                                        GameStore.Message.UpdateIsObjsListUpdated(
                                            state.objectsList.hashCode() to true
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

            is GameStore.Action.StartGameSettingsFlow ->
                scope.launch {
                    settingsRepo.settingsFlow
                        .filter { it.isUseExecString }
                        .collect { dispatch(GameStore.Message.UpdateExecutorStatus(it.isUseExecString)) }
                }

            is GameStore.Action.StartGameDialogFlow ->
                scope.launch {
                    service.gameDialogFlow.collect { items ->
                        val payload = if (items.first == LibTypeDialog.DIALOG_PICTURE) {
                            items.second
                                .normalizeContentPath()
                                .getImageUriFromPath(appContext, state().gameDirUriBox)
                                .toString()
                        } else {
                            items.second
                        }
                        scope.launch {
                            publish(
                                ShowDialog(
                                    payload,
                                    items.first,
                                    items.third
                                )
                            )
                        }
                    }
                }

            is GameStore.Action.StartGameElementVisFlow ->
                scope.launch(Dispatchers.IO) {
                    service.gameElementVis
                        .distinctUntilChanged()
                        .filterNot { (window, _) -> window == LibTypeWindow.ACTS }
                        .collect { (window, bool) ->
                            scope.launch {
                                when (window) {
                                    LibTypeWindow.OBJS -> {
                                        dispatch(GameStore.Message.UpdateVisObjElement(bool))
                                    }

                                    LibTypeWindow.VARS -> {
                                        dispatch(GameStore.Message.UpdateVisExtraElement(bool))
                                    }

                                    LibTypeWindow.INPUT -> {
                                        dispatch(GameStore.Message.UpdateVisInputElement(bool))
                                    }

                                    else -> {
                                        // do nothing
                                    }
                                }
                            }
                        }
                }

            is GameStore.Action.StartGamePopupFlow ->
                scope.launch(Dispatchers.IO) {
                    service.gamePopupFlow.collect { type ->
                        scope.launch {
                            when (type) {
                                LibTypePopup.POPUP_SAVE -> publish(ShowSaveFilePicker)

                                LibTypePopup.POPUP_LOAD -> publish(ShowLoadFilePicker)
                            }
                        }
                    }
                }
        }
    }
}