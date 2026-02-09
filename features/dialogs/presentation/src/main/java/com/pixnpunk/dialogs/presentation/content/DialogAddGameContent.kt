package com.pixnpunk.dialogs.presentation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameCoverSelector(
            imagePath = entry.gameIconPath,
            onClick = selectFile,
            onClear = { deselectFile("") }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = gameName,
            onValueChange = { gameName = it },
            singleLine = true,
            label = { Text(stringResource(R.string.title_field)) }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = gameAuthor,
                onValueChange = { gameAuthor = it },
                singleLine = true,
                label = { Text(stringResource(R.string.author_field)) }
            )
            OutlinedTextField(
                modifier = Modifier.weight(0.5f),
                value = gameVersion,
                onValueChange = { gameVersion = it },
                singleLine = true,
                label = { Text(stringResource(R.string.version_field)) }
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = entry.gameDirPath.isNotEmpty(),
                onClick = if (entry.gameDirPath.isNotEmpty()) deselectDir else selectDir,
                label = {
                    Text(
                        if (entry.gameDirPath.isNotEmpty()) entry.gameDirPath.receiveDirRelPath()
                        else stringResource(R.string.folder_selector_title)
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                trailingIcon = if (entry.gameDirPath.isNotEmpty()) {
                    { Icon(Icons.Default.Close, contentDescription = "Clear") }
                } else null
            )

            if (isAddLocalGame && entry.gameDirPath.isNotEmpty()) {
                entry.gameFilesPath.forEach { filePath ->
                    FilterChip(
                        selected = true,
                        onClick = { deselectFile(filePath) },
                        label = { Text(filePath.receiveFileRelPath()) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null)},
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null) },
                    )
                }

                AssistChip(
                    onClick = selectFiles,
                    label = { Text(stringResource(R.string.file_selector_title)) },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (isAddLocalGame) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel_button_description))
                }
                Spacer(Modifier.width(8.dp))
            }

            Button(
                onClick = {
                    addGame(entry.copy(title = gameName, author = gameAuthor, version = gameVersion))
                },
                enabled = isButtonEnabled
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun GameCoverSelector(
    imagePath: String,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    val imageSize = 120.dp
    val buttonSize = 32.dp
    val badgeOffset = buttonSize / 3

    Box(
        modifier = Modifier.padding(top = badgeOffset, end = badgeOffset),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            onClick = onClick,
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.size(imageSize)
        ) {
            if (imagePath.isNotEmpty()) {
                AsyncImage(
                    model = imagePath,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = imagePath.isNotEmpty(),
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            FilledIconButton(
                onClick = onClear,
                modifier = Modifier
                    .size(buttonSize)
                    .offset(x = badgeOffset, y = -badgeOffset),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove cover",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}