package com.pixnpunk.game.presentation.mvi

import android.content.Context
import androidx.core.net.toUri
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.forceDelete
import com.anggrayudi.storage.file.makeFile
import com.anggrayudi.storage.file.makeFolder
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.database.dao.SaveSlotDao
import com.pixnpunk.database.model.SaveSlotEntity
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.LibSaveItem
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.data.NativeViewModel
import com.pixnpunk.game.domain.model.LibReturnValue
import com.pixnpunk.game.domain.model.LibTypeDialog
import com.pixnpunk.game.domain.model.LibTypePopup
import com.pixnpunk.game.domain.model.LibTypeWindow
import com.pixnpunk.game.presentation.mvi.GameStore.Label.ShowDialog
import com.pixnpunk.game.presentation.mvi.GameStore.Label.ShowLoadFilePicker
import com.pixnpunk.game.presentation.mvi.GameStore.Label.ShowSaveFilePicker
import com.pixnpunk.utils.FileUtil.isWritableDir
import com.pixnpunk.utils.FileUtil.isWritableFile
import com.pixnpunk.utils.PathUtil.getImageUriFromPath
import com.pixnpunk.utils.PathUtil.normalizeContentPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class GameExecutor(
    private val appContext: Context,
    private val service: NativeViewModel,
    private val settingsState: StateFlow<SettingsEntity>,
    private val saveSlotDao: SaveSlotDao
) : CoroutineExecutor<GameStore.Intent, GameStore.Action, GameStore.State, GameStore.Message, GameStore.Label>() {

    @OptIn(ExperimentalContracts::class, ExperimentalUuidApi::class)
    override fun executeIntent(intent: GameStore.Intent) {
        when (intent) {
            is GameStore.Intent.OnDismissDialog -> service.putReturnValue(null)

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

            is GameStore.Intent.OnCorruptFile -> {
                dispatch(GameStore.Message.UpdateIsUserChooseSave(false))
                if (intent.isCancelUser) {
                    service.putReturnValue(null)
                } else {
                    service.putReturnValue(LibReturnValue())
                }
            }

            is GameStore.Intent.OnExecCode -> scope.launch { service.onCodeExec(intent.codeToExec) }

            is GameStore.Intent.CleanMainSavedDesc ->
                dispatch(
                    GameStore.Message.UpdateIsMainDescUpdated(
                        state().isMainDescUpdated.copy(second = false)
                    )
                )

            GameStore.Intent.CleanActionsSavedList ->
                dispatch(
                    GameStore.Message.UpdateIsActionsListUpdated(
                        state().isActionsListUpdated.copy(second = false)
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
                dispatch(GameStore.Message.UpdateIsUserChooseSave(true))
                if (!state().isGameDirInternal) {
                    publish(ShowLoadFilePicker)
                } else {
                    scope.launch(Dispatchers.IO) {
                        saveSlotDao.getAllSaves(state().gameId).map {
                            LibSaveItem(
                                it.userTitle,
                                it.timestamp,
                                it.filePath
                            )
                        }.let {
                            scope.launch {
                                if (it.isEmpty()) {
                                    publish(GameStore.Label.ShowSnackBar("No saved games!"))
                                } else {
                                    publish(GameStore.Label.ShowListSavesDialog(it))
                                }
                            }
                        }
                    }
                }
            }

            is GameStore.Intent.StartSaveFile -> {
                if (!state().isGameDirInternal) {
                    dispatch(GameStore.Message.UpdateIsUserChooseSave(true))
                    publish(ShowSaveFilePicker)
                } else {
                    scope.launch(Dispatchers.IO) {
                        val savesCount = saveSlotDao.getSavesCount(state().gameId)
                        scope.launch {
                            publish(
                                ShowDialog(
                                    inputString = "Save ${savesCount + 1}",
                                    dialogState = LibTypeDialog.DIALOG_SAVEGAME
                                )
                            )
                        }
                    }
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

                    InputState.INPUT_SAVE_NAME -> {
                        val gameDir = state().gameDirUriBox.toUri().toDocumentFile(appContext)
                        if (!gameDir.isWritableDir()) return
                        val savesDir = gameDir.makeFolder(
                            appContext,
                            "QuasporiumSaves",
                            CreateMode.REUSE
                        )
                        if (!savesDir.isWritableDir()) return
                        scope.launch(Dispatchers.IO) {
                            val newSaveFile = savesDir.makeFile(
                                appContext,
                                "Save_${Uuid.generateV4()}_.sav",
                                MimeType.BINARY_FILE,
                                CreateMode.REPLACE
                            )
                            if (!newSaveFile.isWritableFile()) {
                                newSaveFile?.delete()
                            } else {
                                val existSave = saveSlotDao.searchByTitle(
                                    state().gameId,
                                    intent.inputString
                                )
                                if (existSave == null) {
                                    saveSlotDao.insert(
                                        SaveSlotEntity(
                                            gameId = state().gameId,
                                            userTitle = intent.inputString,
                                            filePath = newSaveFile.uri.toString(),
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                } else {
                                    val fileExistSave =
                                        existSave.filePath.toUri().toDocumentFile(appContext)
                                    if (fileExistSave != null) {
                                        fileExistSave.forceDelete(appContext)
                                        saveSlotDao.update(
                                            existSave.copy(
                                                userTitle = intent.inputString,
                                                filePath = newSaveFile.uri.toString(),
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                }
                                scope.launch {
                                    service.onSaveFile(newSaveFile.uri)
                                    publish(GameStore.Label.ShowSnackBar("Game saved!"))
                                }
                            }
                        }
                    }

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
                            dispatch(GameStore.Message.UpdateIsGameRunning(state.gameRunning))

                            if (state.mainDesc.isNotBlank()) {
                                if (state().isMainDescUpdated.first != state.mainDesc.hashCode()) {
                                    dispatch(
                                        GameStore.Message.UpdateIsMainDescUpdated(
                                            state.mainDesc.hashCode() to true
                                        )
                                    )
                                }
                            } else {
                                dispatch(
                                    GameStore.Message.UpdateIsMainDescUpdated(0 to false)
                                )
                            }

                            if (state.varsDesc.isNotBlank()) {
                                if (state().isExtraDescUpdated.first != state.varsDesc.hashCode()) {
                                    dispatch(
                                        GameStore.Message.UpdateIsExtraDescUpdated(
                                            state.varsDesc.hashCode() to true
                                        )
                                    )
                                }
                            } else {
                                dispatch(
                                    GameStore.Message.UpdateIsExtraDescUpdated(0 to false)
                                )
                            }

                            if (state.objectsList.isNotEmpty()) {
                                if (state().isObjectsListUpdated.first != state.objectsList.hashCode()) {
                                    dispatch(
                                        GameStore.Message.UpdateIsObjsListUpdated(
                                            state.objectsList.hashCode() to true
                                        )
                                    )
                                }
                            } else {
                                dispatch(
                                    GameStore.Message.UpdateIsObjsListUpdated(0 to false)
                                )
                            }

                            if (state.actionsList.isNotEmpty()) {
                                if (state().isActionsListUpdated.first != state.actionsList.hashCode()) {
                                    dispatch(
                                        GameStore.Message.UpdateIsActionsListUpdated(
                                            state.actionsList.hashCode() to true
                                        )
                                    )
                                }
                            } else {
                                dispatch(
                                    GameStore.Message.UpdateIsActionsListUpdated(0 to false)
                                )
                            }
                        }
                    }
                }

            is GameStore.Action.StartGameSettingsFlow ->
                scope.launch {
                    settingsState.collect {
                        dispatch(GameStore.Message.UpdateExecutorStatus(it.isUseExecString))
                        dispatch(GameStore.Message.UpdateIsSaveDisable(it.isNoSave))
                    }
                }

            is GameStore.Action.StartGameDialogFlow ->
                scope.launch {
                    service.gameDialogFlow.collect { items ->
                        scope.launch {
                            publish(
                                ShowDialog(
                                    when (items.first) {
                                        LibTypeDialog.DIALOG_PICTURE ->
                                            items.second
                                                .normalizeContentPath()
                                                .getImageUriFromPath(
                                                    appContext,
                                                    state().gameDirUriBox
                                                )
                                                .toString()

                                        else -> items.second
                                    },
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