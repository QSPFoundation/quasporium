package com.pixnpunk.details.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.details.domain.repository.PresetRepository
import com.pixnpunk.details.domain.repository.SavesRepository
import com.pixnpunk.details.presentation.model.GameFileItem
import com.pixnpunk.details.presentation.model.GameSaveItem
import com.pixnpunk.details.presentation.model.PresetUIState
import com.pixnpunk.dto.GameListType
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.stock.domain.usecase.ObserveEntryByIdUseCase
import com.pixnpunk.utils.DirUtil.receiveGameFilesFromDir
import com.pixnpunk.utils.DirUtil.receiveGameSavesFromDir
import com.pixnpunk.utils.FileUtil.receiveBaseName
import com.pixnpunk.utils.FileUtil.receiveFullName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsExecutor(
    private val logger: AppLogger,
    private val observeEntryByIdUseCase: ObserveEntryByIdUseCase,
    private val presetRepository: PresetRepository,
    private val savesRepository: SavesRepository
) : CoroutineExecutor<DetailsStore.Intent, DetailsStore.Action, DetailsStore.State, DetailsStore.Message, DetailsStore.Label>() {

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

        scope.launch(Dispatchers.IO) {
            runCatching {
                launch { syncGameFiles(entry.gameDirPath) }
                launch { syncSaves(entry.id, entry.gameDirPath) }
            }.onFailure {
                logger.recordNonFatalException(it)
            }
        }
    }

    private fun observeDB(entryId: Long) = scope.launch(Dispatchers.Default) {
        combine(
            observeEntryByIdUseCase.execute(entryId).filterNotNull(),
            presetRepository.observeAllPresets(entryId),
            savesRepository.observeAllSaves(entryId)
        ) { gameEntry, profiles, allSaves ->
            gameEntry to profiles.map { profile ->
                PresetUIState(
                    id = profile.id,
                    name = profile.name,
                    lastPlayedFile = profile.lastPlayedFile?.let { path ->
                        GameFileItem(displayName = path.receiveFullName(), path = path)
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
        }.collect { (gameEntry, convProfiles) ->
            withContext(Dispatchers.Main) {
                dispatch(DetailsStore.Message.UpdateEntry(gameEntry))
                dispatch(DetailsStore.Message.UpdateProfiles(convProfiles))
                dispatch(DetailsStore.Message.SetLoading(false))
            }
        }
    }

    private suspend fun syncGameFiles(gameDirPath: String) {
        val files = receiveGameFilesFromDir(
            rootDirPath = gameDirPath
        ).map {
            GameFileItem(
                displayName = if (it.isNotBlank()) it.receiveFullName() else "",
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
    ) {
        val diskPaths = receiveGameSavesFromDir(gameDirPath)
        val dbSaves = savesRepository.getAllSaves(entryId)

        val dbPathsSet = dbSaves.map { it.filePath }.toSet()
        val diskPathsSet = diskPaths.toSet()

        diskPaths
            .filterNot { it in dbPathsSet }
            .forEach { path ->
                savesRepository.insertSave(
                    gameId = entryId,
                    userTitle = path.receiveBaseName(),
                    filePath = path
                )
            }

        dbSaves
            .filterNot { it.filePath in diskPathsSet }
            .forEach { save ->
                savesRepository.deleteSave(save.id)
            }
    }
}
