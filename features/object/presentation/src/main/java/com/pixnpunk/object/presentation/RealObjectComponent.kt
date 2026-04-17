package com.pixnpunk.`object`.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.data.GameNativeEngine
import com.pixnpunk.`object`.presentation.mvi.ObjectStore
import com.pixnpunk.`object`.presentation.mvi.RealObjectStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent

class RealObjectComponent(
    private val componentContext: ComponentContext,
    private val gameNativeEngine: GameNativeEngine,
    override val settingsState: StateFlow<SettingsEntity>
) : ComponentContext by componentContext, ObjectComponent, KoinComponent {
    private val store = instanceKeeper.getStore {
        RealObjectStore(
            storeFactory = DefaultStoreFactory(),
            gameNativeEngine = gameNativeEngine
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<ObjectStore.State> = store.stateFlow
    override val onObjectSelected: (Int) -> Unit = { index ->
        store.accept(ObjectStore.Intent.ObjectSelected(index))
    }
}
