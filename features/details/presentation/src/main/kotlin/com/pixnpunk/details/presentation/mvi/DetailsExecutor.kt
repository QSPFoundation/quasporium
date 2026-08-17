package com.pixnpunk.details.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.details.domain.repository.ProfileRepository
import com.pixnpunk.details.domain.repository.SavesRepository
import com.pixnpunk.details.presentation.model.GameFileItem
import com.pixnpunk.details.presentation.model.GameSaveItem
import com.pixnpunk.details.presentation.model.ProfileUIState
import com.pixnpunk.dto.GameListType
import com.pixnpunk.stock.domain.usecase.ObserveEntryByIdUseCase
import com.pixnpunk.utils.DirUtil.receiveGameFilesFromDir
import com.pixnpunk.utils.FileUtil.receiveFullName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailsExecutor(
    private val observeEntryByIdUseCase: ObserveEntryByIdUseCase,
    private val profileRepository: ProfileRepository,
    private val savesRepository: SavesRepository
) : CoroutineExecutor<DetailsStore.Intent, DetailsStore.Action, DetailsStore.State, DetailsStore.Message, DetailsStore.Label>() {

    override fun executeIntent(intent: DetailsStore.Intent) {
        when (intent) {
            is DetailsStore.Intent.CreateProfile -> scope.launch {
                profileRepository.createProfile(
                    state().gameEntry.id,
                    "Playthrough ${state().gameProfiles.size + 1}"
                )
            }

            is DetailsStore.Intent.UpdateProfileVisuals -> scope.launch {
                profileRepository.updateProfileVisuals(
                    profileId = intent.profileId,
                    name = intent.newName,
                    accentColor = intent.newColor
                )
            }

            is DetailsStore.Intent.SetBackgroundImage -> scope.launch {
                profileRepository.saveBackgroundImage(intent.profileId, intent.uriString)
            }

            is DetailsStore.Intent.RemoveBackgroundImage -> scope.launch {
                profileRepository.removeBackgroundImage(intent.profileId)
            }

            is DetailsStore.Intent.DeleteProfile -> scope.launch {
                profileRepository.deleteProfile(intent.profileId)
            }

            is DetailsStore.Intent.StartFresh -> scope.launch {
                profileRepository.updateLastPlayedFile(intent.profileId, intent.fileName)
                profileRepository.updateProfileTimestamp(intent.profileId)
                publish(DetailsStore.Label.LaunchEngine(intent.profileId, intent.fileName))
            }

            is DetailsStore.Intent.ContinueGame -> {
                val profile = state().gameProfiles.find { it.id == intent.profileId }
                val targetFile =
                    profile?.lastPlayedFile ?: state().gameFiles.firstOrNull() ?: return

                scope.launch {
                    profileRepository.updateProfileTimestamp(intent.profileId)
                    publish(
                        DetailsStore.Label.LaunchEngine(
                            intent.profileId,
                            targetFile.path,
                            intent.savePath
                        )
                    )
                }
            }
        }
    }

    override fun executeAction(action: DetailsStore.Action) {
        if (action is DetailsStore.Action.Init) {
            val entry = action.entry

            if (entry.listId == GameListType.Playable) {
                scope.launch(Dispatchers.IO) {
                    val files = receiveGameFilesFromDir(
                        entry.gameDirPath
                    ).map {
                        GameFileItem(
                            displayName = if (it.isNotBlank()) it.receiveFullName() else "",
                            path = it
                        )
                    }
                    scope.launch {
                        dispatch(DetailsStore.Message.UpdateGameFiles(files))
                    }
                }

                scope.launch(Dispatchers.IO) {
                    observeEntryByIdUseCase
                        .execute(entry.id)
                        .collect { entry ->
                            scope.launch {
                                if (entry != null) {
                                    dispatch(DetailsStore.Message.UpdateEntry(entry))
                                } else {
                                    publish(DetailsStore.Label.NullUpdateReceived)
                                }
                            }
                        }
                }

                scope.launch(Dispatchers.IO) {
                    profileRepository
                        .observeProfiles(entry.id)
                        .collect { profiles ->
                            val convProfile = profiles.map { profile ->
                                ProfileUIState(
                                    id = profile.id,
                                    name = profile.name,
                                    lastPlayedFile =
                                        GameFileItem(
                                            displayName =
                                                profile.lastPlayedFile?.receiveFullName() ?: "",
                                            path =
                                                profile.lastPlayedFile ?: ""
                                        ),
                                    saves =
                                        savesRepository.getAllSaves(
                                            profile.id
                                        ).map {
                                            GameSaveItem(
                                                displayName = it.fileName,
                                                path = it.fileUriBox
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

                            scope.launch {
                                dispatch(DetailsStore.Message.UpdateProfiles(convProfile))
                            }
                        }
                }
            }
        }
    }
}
