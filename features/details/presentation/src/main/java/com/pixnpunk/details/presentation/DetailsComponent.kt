package com.pixnpunk.details.presentation

import com.pixnpunk.details.presentation.mvi.DetailsStore
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.SettingsEntity
import kotlinx.coroutines.flow.StateFlow

interface DetailsComponent {
    val model: StateFlow<DetailsStore.State>
    val settingsState: StateFlow<SettingsEntity>

    val showSelectGameDialog: (GameEntry) -> Unit
    val showEditGameDialog: (GameEntry) -> Unit
    val showDownloadGameDialog: (GameEntry) -> Unit
    val onDismissed: () -> Unit

    fun convEntryData(entry: GameEntry): GameEntry
}