package com.pixnpunk.dialogs.presentation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pixnpunk.dialogs.presentation.R
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.utils.FileUtil.receiveDirName
import com.pixnpunk.utils.FileUtil.receiveDirRelPath
import com.pixnpunk.utils.FileUtil.receiveFileRelPath

@Composable
fun DialogAddGameContent(
    entry: GameEntry,
    selectFile: () -> Unit,
    deselectFile: (String) -> Unit,
    selectDir: () -> Unit,
    deselectDir: () -> Unit,
    selectFiles: () -> Unit,
    addGame: (GameEntry) -> Unit,
    onDismiss: () -> Unit,
    isAddLocalGame: Boolean,
    buttonText: String
) {
    var gameName = entry.title.ifBlank { entry.gameDirPath.receiveDirName() }
    var gameAuthor = entry.author
    var gameVersion = entry.version

    val isButtonEnabled = gameName.isNotBlank() && entry.gameDirPath.isNotEmpty()

    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box {
                IconButton(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(1.dp, contentColor, CircleShape),
                    onClick = selectFile
                ) {
                    if (entry.gameIconPath.isNotEmpty()) {
                        AsyncImage(
                            model = entry.gameIconPath,
                            contentDescription = null
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                        )
                    }
                }
                if (entry.gameIconPath.isNotEmpty()) {
                    IconButton(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp),
                        onClick = { deselectFile("") },
                    ) {
                        Icon(
                            modifier = Modifier.background(backgroundColor),
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                }
            }
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = gameName,
            onValueChange = { gameName = it },
            singleLine = true,
            label = { Text(stringResource(R.string.title_field)) }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = gameAuthor,
            onValueChange = { gameAuthor = it },
            singleLine = true,
            label = { Text(stringResource(R.string.author_field)) }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = gameVersion,
            onValueChange = { gameVersion = it },
            singleLine = true,
            label = { Text(stringResource(R.string.version_field)) }
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                onClick =
                    if (entry.gameDirPath.isNotEmpty()) { deselectDir } else { selectDir },
                label = {
                    if (entry.gameDirPath.isNotEmpty()) {
                        Text(entry.gameDirPath.receiveDirRelPath())
                    } else {
                        Text(stringResource(R.string.folder_selector_title))
                    }
                },
                selected = entry.gameDirPath.isNotEmpty(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null
                    )
                }
            )
            if (isAddLocalGame) {
                AnimatedVisibility(
                    visible = entry.gameDirPath.isNotEmpty()
                ) {
                    if (entry.gameFilesPath.isNotEmpty()) {
                        entry.gameFilesPath.forEach {
                            FilterChip(
                                onClick = { deselectFile(it) },
                                label = { Text(it.receiveFileRelPath()) },
                                selected = true,
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = entry.gameDirPath.isNotEmpty()
                ) {
                    FilterChip(
                        onClick = selectFiles,
                        label = { Text(stringResource(R.string.file_selector_title)) },
                        selected = false,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
        if (!isAddLocalGame) {
            FilledTonalButton(
                onClick = {
                    addGame(
                        entry.copy(
                            title = gameName,
                            author = gameAuthor,
                            version = gameVersion
                        )
                    )
                },
                enabled = isButtonEnabled
            ) {
                Text(buttonText)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = {
                        addGame(
                            entry.copy(
                                title = gameName,
                                author = gameAuthor,
                                version = gameVersion
                            )
                        )
                    },
                    enabled = isButtonEnabled
                ) {
                    Text(buttonText)
                }
                OutlinedButton(
                    onClick = onDismiss
                ) {
                    Text(stringResource(R.string.cancel_button_description))
                }
            }
        }
    }
}