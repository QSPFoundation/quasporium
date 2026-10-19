package com.pixnpunk.stock.presentation.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.ElevatedCard
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
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.pixnpunk.dto.BinaryPrefFormat
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.ui.FormatUtil.formatFileSize

@Composable
fun StockLibraryGameGridItem(
    gameEntry: GameEntry,
    prefFormat: BinaryPrefFormat,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            model =
                ImageRequest.Builder(LocalPlatformContext.current)
                    .data(gameEntry.gameIconPath)
                    .crossfade(true)
                    .build(),
            error = rememberVectorPainter(Icons.Outlined.BrokenImage),
            contentScale = ContentScale.Crop,
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatFileSize(gameEntry.fileSize, prefFormat),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}