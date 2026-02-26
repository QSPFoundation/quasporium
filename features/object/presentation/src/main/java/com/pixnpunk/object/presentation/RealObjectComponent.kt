package com.pixnpunk.`object`.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.`object`.presentation.mvi.ObjectStore
import com.pixnpunk.`object`.presentation.mvi.RealObjectStore
import com.pixnpunk.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealObjectComponent(
    private val componentContext: ComponentContext
) : ComponentContext by componentContext, ObjectComponent, KoinComponent {

    private val store = instanceKeeper.getStore {
        RealObjectStore(DefaultStoreFactory()).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<ObjectStore.State> = store.stateFlow
    override val settingsState: StateFlow<SettingsEntity> =
        get<SettingsRepository>().settingsFlow.stateIn(
            scope = CoroutineScope(Dispatchers.Main),
            started = SharingStarted.Eagerly,
            initialValue = get<SettingsRepository>().currentSettings
        )
    override val onObjectSelected: (Int) -> Unit = { index ->
        store.accept(ObjectStore.Intent.ObjectSelected(index))
    }
}
