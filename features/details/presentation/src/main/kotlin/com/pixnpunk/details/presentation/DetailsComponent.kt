package com.pixnpunk.details.presentation

import com.pixnpunk.details.presentation.mvi.DetailsStore
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.LaunchGameEntity
import com.pixnpunk.dto.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface DetailsComponent {
    val model: StateFlow<DetailsStore.State>
    val label: Flow<DetailsStore.Label>
    val settingsState: StateFlow<SettingsEntity>

    val launchFreshGame: (LaunchGameEntity) -> Unit
    val launchContinueGame: (LaunchGameEntity) -> Unit
    val showEditGameDialog: (GameEntry) -> Unit
    val showSettingsGamePanel: (GameEntry) -> Unit
    val showDownloadGameDialog: (GameEntry) -> Unit
    val onDismissed: () -> Unit

    fun convEntryData(entry: GameEntry): GameEntry

    fun onCreateProfile()
    fun onStartFresh(profileId: Long, fileName: String)
    fun onContinueSave(profileId: Long, savePath: String)
}
