package com.pixnpunk.details.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.details.presentation.mvi.DetailsStore
import com.pixnpunk.details.presentation.mvi.RealDetailsStore
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealDetailsComponent(
    private val componentContext: ComponentContext,
    private val defEntry: GameEntry,
    override val showSelectGameDialog: (GameEntry) -> Unit,
    override val showEditGameDialog: (GameEntry) -> Unit,
    override val showDownloadGameDialog: (GameEntry) -> Unit,
    override val onDismissed: () -> Unit
) : ComponentContext by componentContext, DetailsComponent, KoinComponent {

    override val settingsState: StateFlow<SettingsEntity> =
        get<SettingsRepository>().settingsFlow.stateIn(
            scope = CoroutineScope(Dispatchers.Main),
            started = SharingStarted.Eagerly,
            initialValue = get<SettingsRepository>().currentSettings
        )

    private val store = instanceKeeper.getStore {
        RealDetailsStore(DefaultStoreFactory(), defEntry).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<DetailsStore.State> = store.stateFlow
}