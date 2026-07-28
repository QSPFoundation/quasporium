package com.pixnpunk.details.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
    onShowSettingsGamePanel: () -> Unit,
    onShowDownloadGameDialog: () -> Unit
) {
    Column(
        modifier = Modifier.padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (gameType) {
            GameListType.Playable -> {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onShowSelectGameDialog
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        text = stringResource(R.string.play_button_description)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onShowSettingsGamePanel
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            style = MaterialTheme.typography.titleMedium,
                            text = stringResource(R.string.settings_button_description)
                        )
                    }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onShowEditGameDialog
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            style = MaterialTheme.typography.titleMedium,
                            text = stringResource(R.string.modify_button_description)
                        )
                    }
                }
            }

            GameListType.Downloadable -> {
                FilledTonalButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onShowDownloadGameDialog
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

            else -> {}
        }
    }
}