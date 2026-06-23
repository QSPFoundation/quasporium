package com.pixnpunk.details.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixnpunk.details.presentation.content.DetailsAppBar
import com.pixnpunk.details.presentation.content.DetailsButtonsSet
import com.pixnpunk.details.presentation.content.DetailsDescription
import com.pixnpunk.details.presentation.content.DetailsPrimaryInfo
import com.pixnpunk.details.presentation.content.DetailsSecondaryInfo
import com.pixnpunk.details.presentation.mvi.DetailsStore
import com.pixnpunk.dto.GameListType

@Composable
fun DetailsContent(
    component: DetailsComponent
) {
    val state by component.model.collectAsState()
    val settings by component.settingsState.collectAsState()

    val entry = state.gameEntry

    LaunchedEffect(Unit) {
        component.label.collect {
            when (it) {
                DetailsStore.Label.NullUpdateReceived -> component.onDismissed()
            }
        }
    }

    Scaffold(
        topBar = { DetailsAppBar(component) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailsPrimaryInfo(entry)
            DetailsSecondaryInfo(
                entry = component.convEntryData(entry),
                prefFormat = settings.binaryPref
            )
            DetailsButtonsSet(
                gameType = entry.listId,
                onShowSelectGameDialog = { component.showSelectGameDialog(entry) },
                onShowEditGameDialog = { component.showEditGameDialog(entry) },
                onShowSettingsGamePanel = { component.showSettingsGamePanel(entry) },
                onShowDownloadGameDialog = { component.showDownloadGameDialog(entry) }
            )
            if (entry.listId == GameListType.Downloadable || entry.descUrl.isNotEmpty()) {
                DetailsDescription(entry.descUrl)
            }
        }
    }
}