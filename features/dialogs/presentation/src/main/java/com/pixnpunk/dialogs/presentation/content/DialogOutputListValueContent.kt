package com.pixnpunk.dialogs.presentation.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pixnpunk.dto.LibGenItem

@Composable
fun DialogOutputListValueContent(
    listItems: List<LibGenItem>,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn {
            itemsIndexed(listItems) { key, value ->
                ListItem(
                    modifier = Modifier.clickable(true) {
                        onConfirm(key)
                        onDismiss()
                    },
                    leadingContent = {
                        AsyncImage(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(MaterialTheme.shapes.medium),
                            model = value.imagePath,
                            contentScale = ContentScale.Crop,
                            contentDescription = null
                        )
                    },
                    headlineContent = {
                        Text(
                            text = remember(value.text) {
                                AnnotatedString.fromHtml(value.text)
                            }
                        )
                    }
                )
            }
        }
    }
}