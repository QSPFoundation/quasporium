package com.pixnpunk.game.presentation.mvi

import android.content.Context
import android.content.Intent
import com.anggrayudi.storage.file.MimeType.BINARY_FILE
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
            is GameStore.Intent.OnLoadFile -> scope.launch { service.onLoadFile(intent.fileUri) }

            is GameStore.Intent.OnSaveFile -> scope.launch { service.onSaveFile(intent.fileUri) }

            is GameStore.Intent.OnExecCode -> scope.launch { service.onCodeExec(intent.codeToExec) }

            is GameStore.Intent.CleanCountMainPaneUpdates ->
                dispatch(
                    GameStore.Message.UpdateCountMainPaneUpdates(
                        state().countMainPaneUpdates.mapValues { true }
                    )
                )

            is GameStore.Intent.CleanCountExtraPaneUpdates ->
                dispatch(
                    GameStore.Message.UpdateCountExtraPaneUpdates(
                        state().countExtraPaneUpdates.mapValues { true }
                    )
                )

            is GameStore.Intent.CleanCountObjectsPaneUpdates ->
                dispatch(
                    GameStore.Message.UpdateCountObjectsPaneUpdates(
                        state().countObjectsPaneUpdates.mapValues { true }
                    )
                )

            is GameStore.Intent.OnSelectMenuItem ->
                service.putReturnValue(
                    LibReturnValue(dialogNumValue = intent.index)
                )

            is GameStore.Intent.CreateLoadIntent ->
                publish(
                    ShowLoadFilePicker(
                        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            putExtra(Intent.ACTION_GET_CONTENT, true)
                            setType(BINARY_FILE)
                        }
                    )
                )

            is GameStore.Intent.CreateSaveIntent ->
                publish(
                    ShowSaveFilePicker(
                        Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            putExtra(
                                Intent.EXTRA_TITLE,
                                "${(UInt.MIN_VALUE..UInt.MAX_VALUE).random()}.sav"
                            )
                            setType(BINARY_FILE)
                        }
                    )
                )

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
                                val currentState = state().countMainPaneUpdates
                                dispatch(
                                    GameStore.Message.UpdateCountMainPaneUpdates(
                                        currentState.toMutableMap().apply {
                                            if (!containsKey(state.mainDesc.hashCode())) {
                                                if (size > 3) toMutableMap().clear()
                                                put(state.mainDesc.hashCode(), false)
                                            }
                                        }
                                    )
                                )
                            }
                            if (state.varsDesc.isNotBlank()) {
                                val currentState = state().countExtraPaneUpdates
                                dispatch(
                                    GameStore.Message.UpdateCountExtraPaneUpdates(
                                        currentState.toMutableMap().apply {
                                            if (!containsKey(state.varsDesc.hashCode())) {
                                                if (size > 3) toMutableMap().clear()
                                                put(state.varsDesc.hashCode(), false)
                                            }
                                        }
                                    )
                                )
                            }
                            if (state.objectsList.isNotEmpty()) {
                                val currentState = state().countObjectsPaneUpdates
                                dispatch(
                                    GameStore.Message.UpdateCountObjectsPaneUpdates(
                                        currentState.toMutableMap().apply {
                                            if (!containsKey(state.objectsList.hashCode())) {
                                                if (size > 3) toMutableMap().clear()
                                                put(state.objectsList.hashCode(), false)
                                            }
                                        }
                                    )
                                )
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
                                LibTypePopup.POPUP_SAVE -> {
                                    publish(
                                        ShowSaveFilePicker(
                                            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                                addCategory(Intent.CATEGORY_OPENABLE)
                                                putExtra(
                                                    Intent.EXTRA_TITLE,
                                                    "${(UInt.MIN_VALUE..UInt.MAX_VALUE).random()}.sav"
                                                )
                                                setType(BINARY_FILE)
                                            }
                                        ))

                                }

                                LibTypePopup.POPUP_LOAD -> {
                                    publish(
                                        ShowLoadFilePicker(
                                            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                                addCategory(Intent.CATEGORY_OPENABLE)
                                                putExtra(Intent.ACTION_GET_CONTENT, true)
                                                setType(BINARY_FILE)
                                            }
                                        ))
                                }
                            }
                        }
                    }
                }
        }
    }
}