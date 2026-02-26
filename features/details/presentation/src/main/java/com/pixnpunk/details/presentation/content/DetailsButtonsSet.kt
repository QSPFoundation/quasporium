package com.pixnpunk.details.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixnpunk.details.presentation.R
import com.pixnpunk.dto.GameListType

@Composable
fun DetailsButtonsSet(
    gameType: GameListType,
    onShowSelectGameDialog: () -> Unit,
    onShowEditGameDialog: () -> Unit,
    onShowDownloadGameDialog: () -> Unit
) {
    Row(
        modifier = Modifier.padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (gameType) {
            GameListType.Playable -> {
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = onShowSelectGameDialog
                ) {
                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        text = stringResource(R.string.play_button_description)
                    )
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onShowEditGameDialog
                ) {
                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        text = stringResource(R.string.modify_button_description)
                    )
                }
            }

            GameListType.Downloadable -> {
                FilledTonalButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onShowDownloadGameDialog
                ) {
                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        text = stringResource(R.string.download_button_description)
                    )
                }
            }

            else -> {}
        }
    }
}