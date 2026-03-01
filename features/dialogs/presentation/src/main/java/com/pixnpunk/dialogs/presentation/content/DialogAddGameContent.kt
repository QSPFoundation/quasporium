package com.pixnpunk.dialogs.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixnpunk.dialogs.presentation.R
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.utils.FileUtil.receiveBaseName
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
    var gameName by remember { mutableStateOf(entry.title) }
    var gameAuthor by remember { mutableStateOf(entry.author) }
    var gameVersion by remember { mutableStateOf(entry.version) }

    if (gameName.isBlank() && entry.gameDirPath.isNotBlank()) {
        gameName = entry.gameDirPath.receiveBaseName()
    }

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

        if (isAddLocalGame) {
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
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = if (entry.gameDirPath.isNotEmpty()) {
                        { Icon(Icons.Default.Close, contentDescription = "Clear") }
                    } else null
                )

                if (entry.gameDirPath.isNotEmpty()) {
                    entry.gameFilesPath.forEach { filePath ->
                        FilterChip(
                            selected = true,
                            onClick = { deselectFile(filePath) },
                            label = { Text(filePath.receiveFileRelPath()) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.InsertDriveFile,
                                    contentDescription = null
                                )
                            },
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
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button_description))
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    addGame(
                        entry.copy(
                            title = gameName,
                            author = gameAuthor,
                            version = gameVersion
                        )
                    )
                },
                enabled =
                    if (isAddLocalGame) {
                        gameName.isNotBlank() && entry.gameDirPath.isNotEmpty()
                    } else {
                        gameName.isNotBlank()
                    }
            ) {
                Text(buttonText)
            }
        }
    }
}