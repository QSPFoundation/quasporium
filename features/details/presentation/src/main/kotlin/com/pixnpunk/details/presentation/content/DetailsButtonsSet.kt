package com.pixnpunk.details.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixnpunk.details.presentation.R
import com.pixnpunk.details.presentation.content.profile.DetailsProfileCarousel
import com.pixnpunk.details.presentation.model.GameFileItem
import com.pixnpunk.details.presentation.model.ProfileUIState
import com.pixnpunk.dto.GameListType

@Composable
fun DetailsButtonsSet(
    gameType: GameListType,
    profiles: List<ProfileUIState>,
    files: List<GameFileItem>,
    onCreateProfile: () -> Unit,
    onDownload: () -> Unit,
    onContinue: (profileId: Long, savePath: String) -> Unit,
    onStartFresh: (profileId: Long, fileName: String) -> Unit
) {
    Column(
        modifier = Modifier.padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (gameType) {
            GameListType.Unknown -> {}

            GameListType.Playable -> {
                DetailsProfileCarousel(
                    profiles = profiles,
                    files = files,
                    onCreateProfile = onCreateProfile,
                    onContinue = onContinue,
                    onStartFresh = onStartFresh
                )
            }

            GameListType.Downloadable -> {
                FilledTonalButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDownload
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        text = stringResource(R.string.download_button_description)
                    )
                }
            }
        }
    }
}