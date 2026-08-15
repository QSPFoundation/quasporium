package com.pixnpunk.`object`.presentation.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pixnpunk.`object`.presentation.model.ParsedObjectItem

@Composable
fun ObjectCell(
    modifier: Modifier = Modifier,
    item: ParsedObjectItem,
    textStyle: TextStyle,
    backColor: Color,
    onItemClick: () -> Unit
) {
    ListItem(
        modifier = modifier.clickable { onItemClick() },
        colors = ListItemDefaults.colors(
            containerColor = backColor
        ),
        leadingContent = {
            val imagePath = item.original.imagePath
            if (imagePath != null) {
                AsyncImage(
                    model = imagePath,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
            }
        },
        headlineContent = {
            Text(
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = item.annotatedText.text
                },
                text = item.annotatedText,
                style = textStyle
            )
        }
    )
}