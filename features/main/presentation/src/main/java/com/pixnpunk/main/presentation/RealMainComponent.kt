package com.pixnpunk.main.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.main.presentation.mvi.MainStore
import com.pixnpunk.main.presentation.mvi.RealMainStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent

class RealMainComponent(
    private val componentContext: ComponentContext,
    override val settingsState: StateFlow<SettingsEntity>,
    override val gameDirUriBox: String,
    override val onExecValue: (String) -> Unit
) : ComponentContext by componentContext, MainComponent, KoinComponent {

    private val store = instanceKeeper.getStore {
        RealMainStore(
            storeFactory = DefaultStoreFactory(),
            stateKeeper = stateKeeper,
            settingsState = settingsState
        ).create()
    }

    init {
        stateKeeper.register(
            key = "MainStoreSavedState",
            strategy = MainStore.State.serializer(),
            supplier = { store.state }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<MainStore.State> = store.stateFlow
    override val onActionClicked: (Int) -> Unit = { index ->
        store.accept(MainStore.Intent.ActionClick(index))
    }
}
