package com.pixnpunk.dialogs.presentation.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.utils.FileUtil.receiveRelPath
import kotlinx.io.files.Path

@Composable
fun DialogAddGameContent(
    entry: GameEntry,
    selectFile: () -> Unit,
    deselectFile: () -> Unit,
    selectFolder: () -> Unit,
    addGame: (GameEntry) -> Unit,
    buttonText: String
) {
    var gameName = entry.title.ifBlank { Path(entry.gameDirPath).name }
    var gameAuthor = entry.author
    var gameVersion = entry.version

    val isButtonEnabled = gameName.isNotBlank() && entry.gameDirPath.isNotEmpty()

    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
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
                        onClick = deselectFile,
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
            label = { Text("Game name") }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = gameAuthor,
            onValueChange = { gameAuthor = it },
            singleLine = true,
            label = { Text("Game author") }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = gameVersion,
            onValueChange = { gameVersion = it },
            singleLine = true,
            label = { Text("Game version") }
        )
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = selectFolder
        ) {
            if (entry.gameDirPath.isNotEmpty()) {
                Text(entry.gameDirPath.receiveRelPath())
            } else {
                Text("Select game folder")
            }
        }
        FilledTonalButton(
            modifier = Modifier.fillMaxWidth(),
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
    }
}