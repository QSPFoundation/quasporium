package com.pixnpunk.details.presentation

import com.pixnpunk.details.presentation.mvi.DetailsStore
import com.pixnpunk.dto.GameEntry
import kotlinx.coroutines.flow.StateFlow

interface DetailsComponent {
    val model: StateFlow<DetailsStore.State>

    val showSelectGameDialog: (GameEntry) -> Unit
    val showEditGameDialog: (GameEntry) -> Unit
    val showDownloadGameDialog: (GameEntry) -> Unit
    val onDismissed: () -> Unit

    fun subscribeEntry(entryId: Long)
}