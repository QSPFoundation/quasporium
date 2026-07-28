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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.pixnpunk.dialogs.presentation.R
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.utils.FileUtil.receiveFileRelPath

@Composable
fun DialogEditGameContent(
    entry: GameEntry,
    selectFile: () -> Unit,
    deselectFile: (String) -> Unit,
    selectFiles: () -> Unit,
    editGame: (GameEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    var gameName by remember { mutableStateOf(entry.title) }
    var gameAuthor by remember { mutableStateOf(entry.author) }
    var gameVersion by remember { mutableStateOf(entry.version) }

    val isButtonEnabled = gameName.isNotBlank() && entry.gameDirPath.isNotEmpty()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
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
            label = { Text(stringResource(R.string.title_field)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            )
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
                label = { Text(stringResource(R.string.author_field)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                modifier = Modifier.weight(0.5f),
                value = gameVersion,
                onValueChange = { gameVersion = it },
                singleLine = true,
                label = { Text(stringResource(R.string.version_field)) },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
        }

        if (!entry.gameDirPath.startsWith("file", true)) {
            HorizontalDivider()

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (entry.gameDirPath.isNotEmpty()) {
                    entry.gameFilesPath.forEach { filePath ->
                        InputChip(
                            selected = true,
                            onClick = { deselectFile(filePath) },
                            label = { Text(filePath.receiveFileRelPath()) },
                            avatar = {
                                Icon(
                                    Icons.AutoMirrored.Filled.InsertDriveFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
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
                    editGame(
                        entry.copy(
                            title = gameName,
                            author = gameAuthor,
                            version = gameVersion
                        )
                    )
                },
                enabled = isButtonEnabled
            ) {
                Text(stringResource(R.string.edit_button_description))
            }
        }
    }
}