package com.pixnpunk.dialogs.presentation.content.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.storage.FileUtil.receiveBaseName

@Composable
fun DialogAddGameContent(
    entry: GameEntry,
    selectDir: () -> Unit,
    onCoverSelect: () -> Unit,
    onCoverClear: () -> Unit,
    addGame: (GameEntry) -> Unit,
    onDismiss: () -> Unit,
    isAddLocalGame: Boolean,
    buttonText: String
) {
    var gameName by rememberSaveable(entry.title) { mutableStateOf(entry.title) }
    var gameAuthor by rememberSaveable(entry.author) { mutableStateOf(entry.author) }
    var gameVersion by rememberSaveable(entry.version) { mutableStateOf(entry.version) }

    val isDirSelected = entry.gameDirPath.isNotEmpty()

    LaunchedEffect(entry.gameDirPath) {
        if (gameName.isEmpty() && entry.gameDirPath.isNotBlank()) {
            gameName = entry.gameDirPath.receiveBaseName()
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AddGameFormInputs(
            imagePath = entry.gameIconPath,
            name = gameName,
            author = gameAuthor,
            version = gameVersion,
            onNameChange = { gameName = it },
            onAuthorChange = { gameAuthor = it },
            onVersionChange = { gameVersion = it },
            onCoverSelect = onCoverSelect,
            onCoverClear = onCoverClear
        )

        if (isAddLocalGame) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            AddGameFolderSection(
                isDirSelected = isDirSelected,
                gameDirPath = entry.gameDirPath,
                onSelectDir = selectDir
            )
        }

        Spacer(Modifier.height(8.dp))

        AddGameActionButtons(
            buttonText = buttonText,
            isConfirmEnabled =
                if (isAddLocalGame) {
                    gameName.isNotBlank() && entry.gameDirPath.isNotEmpty()
                } else {
                    gameName.isNotBlank()
                },
            onDismiss = onDismiss,
            onConfirm = {
                addGame(
                    entry.copy(
                        title = gameName,
                        author = gameAuthor,
                        version = gameVersion
                    )
                )
            }
        )
    }
}