package com.pixnpunk.`object`.presentation

import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.`object`.presentation.mvi.ObjectStore
import kotlinx.coroutines.flow.StateFlow

interface ObjectComponent {
    val model: StateFlow<ObjectStore.State>
    val settingsState: StateFlow<SettingsEntity>

    val onObjectSelected: (Int) -> Unit
}