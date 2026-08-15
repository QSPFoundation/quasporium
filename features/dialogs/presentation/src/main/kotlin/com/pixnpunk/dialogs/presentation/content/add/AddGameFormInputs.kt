package com.pixnpunk.dialogs.presentation.content.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.pixnpunk.dialogs.presentation.R
import com.pixnpunk.dialogs.presentation.content.GameCoverSelector

@Composable
internal fun AddGameFormInputs(
    imagePath: String,
    name: String,
    author: String,
    version: String,
    onNameChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onVersionChange: (String) -> Unit,
    onCoverSelect: () -> Unit,
    onCoverClear: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameCoverSelector(
            imagePath = imagePath,
            onClick = onCoverSelect,
            onClear = onCoverClear
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            label = { Text(stringResource(R.string.title_field)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = author,
                onValueChange = onAuthorChange,
                singleLine = true,
                label = { Text(stringResource(R.string.author_field)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Right) }
                )
            )

            OutlinedTextField(
                modifier = Modifier.weight(0.6f),
                value = version,
                onValueChange = onVersionChange,
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
    }
}