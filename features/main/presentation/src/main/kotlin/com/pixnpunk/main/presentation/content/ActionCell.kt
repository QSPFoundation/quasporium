package com.pixnpunk.main.presentation.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pixnpunk.main.presentation.model.ParsedActionItem

@Composable
fun ActionCell(
    modifier: Modifier = Modifier,
    item: ParsedActionItem,
    textStyle: TextStyle,
    cardBackgroundColor: Color,
    onItemClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clearAndSetSemantics { contentDescription = item.annotatedText.text },
        onClick = onItemClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val imagePath = item.original.imagePath
            if (imagePath != null) {
                AsyncImage(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentScale = ContentScale.Crop,
                    model = imagePath.value,
                    contentDescription = null
                )
            }

            Text(
                text = item.annotatedText,
                style = textStyle,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
