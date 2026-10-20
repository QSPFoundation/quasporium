package com.pixnpunk.details.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.details.domain.repository.PresetRepository
import com.pixnpunk.details.domain.repository.SavesRepository
import com.pixnpunk.details.presentation.model.GameFileItem
import com.pixnpunk.details.presentation.model.GameSaveItem
import com.pixnpunk.details.presentation.model.PresetUIState
import com.pixnpunk.dto.GameListType
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.logger.executorContext
import com.pixnpunk.settings.domain.repository.GameSettingsPreloader
import com.pixnpunk.stock.domain.usecase.ObserveEntryByIdUseCase
import com.pixnpunk.storage.GameFileStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class DetailsExecutor(
    private val logger: AppLogger,
    private val observeEntryByIdUseCase: ObserveEntryByIdUseCase,
    private val presetRepository: PresetRepository,
    private val savesRepository: SavesRepository,
    private val gameFileStorage: GameFileStorage,
    private val gameSettingsPreloader: GameSettingsPreloader,
    mainContext: CoroutineContext = logger.executorContext("DetailsExecutor"),
    private val ioContext: CoroutineContext = Dispatchers.IO
) : CoroutineExecutor<DetailsStore.Intent, DetailsStore.Action, DetailsStore.State, DetailsStore.Message, DetailsStore.Label>(
    mainContext
) {

    override fun executeIntent(intent: DetailsStore.Intent) {
        when (intent) {
            is DetailsStore.Intent.CreateProfile ->
                scope.launch {
                    presetRepository.createPreset(
                        state().gameEntry.id,
                        "Playthrough ${state().gameProfiles.size + 1}"
                    )
                }

            is DetailsStore.Intent.UpdateProfileVisuals ->
                scope.launch {
                    presetRepository.updateVisuals(
                        presetId = intent.presetId,
                        name = intent.newName,
                        accentColor = intent.newColor
                    )
                }

            is DetailsStore.Intent.SetBackgroundImage ->
                scope.launch {
                    presetRepository.saveBackgroundImage(
                        presetId = intent.presetId,
                        sourceUriBox = intent.uriString
                    )
                }

            is DetailsStore.Intent.RemoveBackgroundImage ->
                scope.launch {
                    presetRepository.removeBackgroundImage(
                        presetId = intent.presetId
                    )
                }

            is DetailsStore.Intent.DeleteProfile ->
                scope.launch {
                    presetRepository.deletePreset(
                        presetId = intent.presetId
                    )
                }

            is DetailsStore.Intent.StartFresh ->
                scope.launch {
                    presetRepository.updateLastPlayedFile(
                        presetId = intent.presetId,
                        fileName = intent.fileName
                    )
                    presetRepository.updateTimestamp(
                        presetId = intent.presetId
                    )
                    publish(
                        DetailsStore.Label.LaunchEngine(
                            presetId = intent.presetId,
                            entryFile = intent.fileName
                        )
                    )
                }

            is DetailsStore.Intent.ContinueGame -> {
                val preset = state().gameProfiles.find { it.id == intent.presetId }
                val targetFile =
                    preset?.lastPlayedFile ?: state().gameFiles.firstOrNull() ?: return

                scope.launch {
                    presetRepository.updateTimestamp(
                        presetId = intent.presetId
                    )
                    publish(
                        DetailsStore.Label.LaunchEngine(
                            presetId = intent.presetId,
                            entryFile = targetFile.path,
                            saveFile = intent.savePath
                        )
                    )
                }
            }
        }
    }

    override fun executeAction(action: DetailsStore.Action) {
        if (action !is DetailsStore.Action.Init) return
        val entry = action.entry
        if (entry.listId != GameListType.Playable) return

        observeDB(entry.id)

        scope.launch(ioContext) { syncGameFiles(entry.gameDirPath) }
        scope.launch(ioContext) { syncSaves(entry.id, entry.gameDirPath) }
        scope.launch(ioContext) { gameSettingsPreloader.preloadGameSettings(entry.id) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDB(entryId: Long) = scope.launch(ioContext) {
        observeEntryByIdUseCase.execute(entryId)
            .flatMapLatest { gameEntry ->
                if (gameEntry == null) {
                    flowOf(null)
                } else {
                    combine(
                        presetRepository.observeAllPresets(entryId),
                        savesRepository.observeAllSaves(entryId)
                    ) { profiles, allSaves ->
                        gameEntry to profiles.map { profile ->
                            PresetUIState(
                                id = profile.id,
                                name = profile.name,
                                lastPlayedFile = profile.lastPlayedFile?.let { path ->
                                    GameFileItem(displayName = gameFileStorage.receiveFullName(path), path = path)
                                },
                                saves =
                                    allSaves.map { save ->
                                        GameSaveItem(
                                            displayName = save.userTitle,
                                            path = save.filePath
                                        )
                                    },
                                accentColor =
                                    if (profile.accentColor != 0L) {
                                        profile.accentColor
                                    } else {
                                        val defaultPresets = longArrayOf(
                                            0xFFD9534FL, 0xFF3B9E9BL, 0xFF8E6FB9L, 0xFFD48B38L
                                        )
                                        defaultPresets[
                                            (profile.id % defaultPresets.size).toInt()
                                                .coerceAtLeast(0)
                                        ]
                                    },
                                backImagePath = profile.backImagePath
                            )
                        }
                    }
                }
            }.collect { result ->
                withContext(Dispatchers.Main) {
                    if (result == null) {
                        publish(DetailsStore.Label.NullUpdateReceived)
                    } else {
                        val (gameEntry, convProfiles) = result
                        dispatch(DetailsStore.Message.UpdateEntry(gameEntry))
                        dispatch(DetailsStore.Message.UpdateProfiles(convProfiles))
                        dispatch(DetailsStore.Message.SetLoading(false))
                    }
                }
            }
    }

    private suspend fun syncGameFiles(gameDirPath: String) {
        val files = gameFileStorage.receiveGameFiles(
            rootDirPath = gameDirPath
        ).map {
            GameFileItem(
                displayName = if (it.isNotBlank()) gameFileStorage.receiveFullName(it) else "",
                path = it
            )
        }
        withContext(Dispatchers.Main) {
            dispatch(DetailsStore.Message.UpdateGameFiles(files))
        }
    }

    private suspend fun syncSaves(
        entryId: Long,
        gameDirPath: String
    ) = planSavesSync(
        diskPaths = gameFileStorage.receiveGameSaves(gameDirPath),
        dbSaves = savesRepository.getAllSaves(entryId)
    ).apply {
        pathsToInsert.forEach { path ->
            savesRepository.insertSave(
                gameId = entryId,
                userTitle = gameFileStorage.receiveBaseName(path),
                filePath = path
            )
        }

        idsToDelete.forEach {
            savesRepository.deleteSave(it)
        }
    }
}
