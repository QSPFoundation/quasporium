package com.pixnpunk.quasporium

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.pixnpunk.details.presentation.DetailsComponent
import com.pixnpunk.dialogs.presentation.DialogsComponent
import com.pixnpunk.quasporium.mvi.RootStore
import com.pixnpunk.settings.presentation.SettingsComponent
import com.pixnpunk.stock.presentation.StockComponent
import kotlinx.coroutines.flow.Flow

sealed interface RootComponent {
    val dialogSlot: Value<ChildSlot<*, DialogsComponent>>

    @OptIn(ExperimentalDecomposeApi::class)
    val childPanel: Value<ChildPanels<*, StockComponent, *, DetailsComponent, *, SettingsComponent>>

    val label: Flow<RootStore.Label>

    @OptIn(ExperimentalDecomposeApi::class)
    fun setMode(mode: ChildPanelsMode)
}