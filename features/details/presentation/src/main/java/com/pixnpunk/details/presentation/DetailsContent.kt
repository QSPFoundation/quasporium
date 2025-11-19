package com.pixnpunk.details.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixnpunk.details.presentation.content.DetailsAppBar
import com.pixnpunk.details.presentation.content.DetailsButtonsSet
import com.pixnpunk.details.presentation.content.DetailsDescription
import com.pixnpunk.details.presentation.content.DetailsPrimaryInfo
import com.pixnpunk.details.presentation.content.DetailsSecondaryInfo
import com.pixnpunk.dto.BinaryPrefFormat
import com.pixnpunk.dto.GameListType
import com.pixnpunk.dto.SettingsEntity

@Composable
fun DetailsContent(
    component: DetailsComponent,
    settings: SettingsEntity
) {
    val entry = component.entryShow

    val numCountInfo =
        when (settings.binaryPref) {
            BinaryPrefFormat.SI -> 1000
            BinaryPrefFormat.ISO -> 1024
        }

    Scaffold(
        topBar = { DetailsAppBar(component) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailsPrimaryInfo(entry)
            DetailsSecondaryInfo(entry, numCountInfo)
            DetailsButtonsSet(
                gameType = entry.listId,
                onShowSelectGameDialog = { component.showSelectGameDialog(entry) },
                onShowEditGameDialog = { component.showEditGameDialog(entry) },
                onShowDownloadGameDialog = { component.showDownloadGameDialog(entry) }
            )
            if (entry.listId == GameListType.Downloadable) {
                DetailsDescription(entry.descUrl)
            }
        }
    }
}