package com.pixnpunk.stock.presentation.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberConstraintsSizeResolver
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.utils.FileUtil.formatFileSize

@Composable
fun StockLibraryGameGridItem(
    gameEntry: GameEntry,
    numCountInfo: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.padding(8.dp),
        onClick = onClick
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(194.dp),
            model =
                ImageRequest.Builder(LocalPlatformContext.current)
                    .data(gameEntry.gameIconPath)
                    .crossfade(true)
                    .size(rememberConstraintsSizeResolver())
                    .build(),
            error = rememberVectorPainter(Icons.Outlined.BrokenImage),
            contentScale = ContentScale.FillBounds,
            contentDescription = null
        )
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = gameEntry.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
            if (gameEntry.fileSize != 0L) {
                Text(
                    text = formatFileSize(gameEntry.fileSize, numCountInfo),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}