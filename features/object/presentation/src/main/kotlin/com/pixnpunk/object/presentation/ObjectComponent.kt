package com.pixnpunk.`object`.presentation

import com.pixnpunk.`object`.presentation.model.ObjectUIState
import kotlinx.coroutines.flow.StateFlow

interface ObjectComponent {
    val uiState: StateFlow<ObjectUIState>
    val onObjectSelected: (Int) -> Unit
}
