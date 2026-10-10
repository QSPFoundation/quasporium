package com.pixnpunk.dialogs.presentation.content.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.pixnpunk.dialogs.presentation.R
import com.pixnpunk.dialogs.presentation.content.GameCoverSelector
import com.pixnpunk.dto.GameEntry

@Composable
fun DialogEditGameContent(
    entry: GameEntry,
    selectCover: () -> Unit,
    deselectCover: () -> Unit,
    editGame: (GameEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    var gameName by rememberSaveable(entry.title) { mutableStateOf(entry.title) }
    var gameAuthor by rememberSaveable(entry.author) { mutableStateOf(entry.author) }
    var gameVersion by rememberSaveable(entry.version) { mutableStateOf(entry.version) }

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
            onClick = selectCover,
            onClear = deselectCover
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