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
import com.pixnpunk.dto.LaunchGameEntity

@Composable
fun DetailsContent(
    component: DetailsComponent
) {
    val state by component.model.collectAsState()
    val settings by component.settingsState.collectAsState()

    val entry = state.gameEntry

    LaunchedEffect(component.label) {
        component.label.collect {
            when (it) {
                is DetailsStore.Label.NullUpdateReceived -> component.onDismissed()
                is DetailsStore.Label.LaunchEngine -> {
                    if (it.saveFile == null) {
                        component.launchFreshGame(
                            LaunchGameEntity(
                                entryFile = it.entryFile,
                                gameEntry = entry
                            )
                        )
                    } else {
                        component.launchContinueGame(
                            LaunchGameEntity(
                                entryFile = it.entryFile,
                                saveFile = it.saveFile,
                                gameEntry = entry
                            )
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            DetailsAppBar(
                isPlayable = entry.listId == GameListType.Playable,
                onDismissed = component.onDismissed,
                onShowSettings = { component.showSettingsGamePanel(entry) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailsPrimaryInfo(
                isPlayable = entry.listId == GameListType.Playable,
                iconPath = entry.gameIconPath,
                title = entry.title,
                author = entry.author,
                portedBy = entry.portedBy,
                onShowEdit = { component.showEditGameDialog(entry) }
            )
            DetailsSecondaryInfo(
                entry = component.convEntryData(entry),
                prefFormat = settings.binaryPref
            )
            DetailsButtonsSet(
                isLoading = state.isLoading,
                gameType = entry.listId,
                presets = state.gameProfiles,
                files = state.gameFiles,
                onCreateProfile = component::onCreateProfile,
                onUpdateVisuals = component::onUpdateVisuals,
                onPickBackground = component::onPickBackground,
                onRemoveBackground = component::onRemoveBackground,
                onDeleteProfile = component::onDeleteProfile,
                onDownload = { component.showDownloadGameDialog(entry) },
                onContinue = component::onContinueSave,
                onStartFresh = component::onStartFresh,
            )
            if (entry.listId == GameListType.Downloadable || entry.descUrl.isNotEmpty()) {
                DetailsDescription(entry.descUrl)
            }
        }
    }
}