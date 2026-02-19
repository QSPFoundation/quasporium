package com.pixnpunk.details.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.details.presentation.mvi.DetailsStore
import com.pixnpunk.details.presentation.mvi.RealDetailsStore
import com.pixnpunk.dto.GameEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class RealDetailsComponent(
    private val componentContext: ComponentContext,
    override val showSelectGameDialog: (GameEntry) -> Unit,
    override val showEditGameDialog: (GameEntry) -> Unit,
    override val showDownloadGameDialog: (GameEntry) -> Unit,
    override val onDismissed: () -> Unit
) : ComponentContext by componentContext, DetailsComponent {

    private val store = instanceKeeper.getStore {
        RealDetailsStore(DefaultStoreFactory()).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<DetailsStore.State> = store.stateFlow

    override fun subscribeEntry(entryId: Long) {
        store.accept(DetailsStore.Intent.SubscribeEntry(entryId))
    }
}