package com.pixnpunk.`object`.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.`object`.presentation.mvi.ObjectStore
import com.pixnpunk.`object`.presentation.mvi.RealObjectStore
import com.pixnpunk.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RealObjectComponent(
    private val componentContext: ComponentContext
) : ComponentContext by componentContext, ObjectComponent, KoinComponent {

    private val settingsRepo: SettingsRepository by inject()
    private val store = instanceKeeper.getStore {
        RealObjectStore(DefaultStoreFactory()).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<ObjectStore.State> = store.stateFlow
    override val settingsFlow = settingsRepo.settingsFlow
    override val onObjectSelected: (Int) -> Unit = { index ->
        store.accept(ObjectStore.Intent.ObjectSelected(index))
    }
}
