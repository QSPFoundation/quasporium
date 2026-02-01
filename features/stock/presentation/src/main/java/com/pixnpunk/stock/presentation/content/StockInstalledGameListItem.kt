package com.pixnpunk.stock.presentation.content

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.utils.FileUtil.formatFileSize

@Composable
fun StockInstalledGameListItem(
    gameEntry: GameEntry,
    numCountInfo: Int,
    isSelected: Boolean,
    deleteModeActive: Boolean,
    onToggleSelection: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .selectable(
                selected = isSelected,
                onClick = onToggleSelection
            )
            .combinedClickable(
                onClick = {
                    if (deleteModeActive) {
                        onToggleSelection()
                    } else {
                        onClick()
                    }
                },
                onLongClick = onLongClick
            ),
        headlineContent = { Text(gameEntry.title) },
        supportingContent = {
            if (gameEntry.fileSize != 0L) {
                Text(formatFileSize(gameEntry.fileSize, numCountInfo))
            }
        },
        leadingContent = {
            AsyncImage(
                modifier = Modifier.size(50.dp),
                model = gameEntry.gameIconPath,
                error = rememberVectorPainter(Icons.Outlined.BrokenImage),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.background
            }
        )
    )
}