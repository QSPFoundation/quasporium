package com.pixnpunk.game.presentation.mvi

import android.content.Context
import android.content.Intent
import com.anggrayudi.storage.file.MimeType.BINARY_FILE
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.dto.LibReturnValue
import com.pixnpunk.dto.LibTypeDialog
import com.pixnpunk.dto.LibTypePopup
import com.pixnpunk.dto.LibTypeWindow
import com.pixnpunk.game.presentation.mvi.GameStore.Label.ShowDialog
import com.pixnpunk.natives.SupervisorViewModel
import com.pixnpunk.settings.SettingsRepository
import com.pixnpunk.utils.PathUtil.getImageUriFromPath
import com.pixnpunk.utils.PathUtil.normalizeContentPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch

internal class GameExecutor(
    private val appContext: Context,
    private val service: SupervisorViewModel,
    private val settingsRepo: SettingsRepository
) : CoroutineExecutor<GameStore.Intent, GameStore.Action, GameStore.State, GameStore.Message, GameStore.Label>() {
    private val outScope = CoroutineScope(Dispatchers.Default)

    override fun executeIntent(intent: GameStore.Intent) {
        when (intent) {
            is GameStore.Intent.CreateLoadIntent -> {
                publish(
                    GameStore.Label.ShowLoadFilePicker(
                        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            putExtra(Intent.ACTION_GET_CONTENT, true)
                            setType(BINARY_FILE)
                        }
                    ))
            }

            is GameStore.Intent.CreateSaveIntent -> {
                publish(
                    GameStore.Label.ShowSaveFilePicker(
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

            is GameStore.Intent.OnLoadFile -> {
                outScope.launch {
                    service.onLoadFile(intent.fileUri)
                }
            }

            is GameStore.Intent.OnSaveFile -> {
                outScope.launch {
                    service.onSaveFile(intent.fileUri)
                }
            }

            is GameStore.Intent.OnExecCode -> {
                outScope.launch {
                    service.onCodeExec(intent.codeToExec)
                }
            }

            is GameStore.Intent.OnSelectMenuItem -> {
                service.putReturnValue(
                    LibReturnValue(dialogNumValue = intent.index)
                )
            }

            is GameStore.Intent.OnEnterValue -> {
                if (intent.isBox) {
                    service.putReturnValue(
                        LibReturnValue(dialogTextValue = intent.inputString)
                    )
                } else {
                    service.onUseInputArea(inputString = intent.inputString)
                }
            }

            is GameStore.Intent.StartGameDialogFlow -> {
                outScope.launch {
                    service.gameDialogFlow.collect { items ->
                        val payload = if (items.first == LibTypeDialog.DIALOG_PICTURE) {
                            items.second
                                .normalizeContentPath()
                                .getImageUriFromPath(appContext, intent.rootDir)
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
            }
        }
    }

    override fun executeAction(action: GameStore.Action) {
        when (action) {
            is GameStore.Action.StartGameStateFlow -> {
                outScope.launch {
                    service.gameStateFlow.collect { state ->
                        scope.launch {
                            dispatch(GameStore.Message.UpdateGameStatus(state.gameRunning))
                        }
                    }
                }
            }

            is GameStore.Action.StartGamePopupFlow -> {
                outScope.launch {
                    service.gamePopupFlow.collect { type ->
                        when (type) {
                            LibTypePopup.POPUP_SAVE -> {
                                scope.launch {
                                    publish(
                                        GameStore.Label.ShowSaveFilePicker(
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
                            }

                            LibTypePopup.POPUP_LOAD -> {
                                scope.launch {
                                    publish(
                                        GameStore.Label.ShowLoadFilePicker(
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

            is GameStore.Action.StartGameElementVisFlow -> {
                outScope.launch {
                    service.gameElementVis
                        .filterNot { (window, _) -> window == LibTypeWindow.ACTS }
                        .collect { (window, bool) ->
                            when (window) {
                                LibTypeWindow.OBJS -> {
                                    scope.launch {
                                        dispatch(GameStore.Message.UpdateVisObjElement(bool))
                                    }
                                }

                                LibTypeWindow.VARS -> {
                                    scope.launch {
                                        dispatch(GameStore.Message.UpdateVisExtraElement(bool))
                                    }
                                }

                                LibTypeWindow.INPUT -> {
                                    scope.launch {
                                        dispatch(GameStore.Message.UpdateVisInputElement(bool))
                                    }
                                }

                                else -> {
                                    // do nothing
                                }
                            }
                        }
                }
            }

            is GameStore.Action.StartGameSettingsFlow -> {
                outScope.launch {
                    settingsRepo.settingsFlow
                        .filter { it.isUseExecString }
                        .collect { dispatch(GameStore.Message.UpdateExecutorStatus(it.isUseExecString)) }
                }
            }
        }
    }

    override fun dispose() {
        super.dispose()
        outScope.cancel()
    }
}