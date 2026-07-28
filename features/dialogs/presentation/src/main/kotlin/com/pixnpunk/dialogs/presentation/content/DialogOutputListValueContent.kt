package com.pixnpunk.dialogs.presentation.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.LibSaveItem
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

@Composable
fun DialogOutputListValueContent(
    menuItems: List<LibGenItem> = emptyList(),
    saveItems: List<LibSaveItem> = emptyList(),
    onConfirm: (Int) -> Unit = { },
    onConfirmFile: (String) -> Unit = { },
    onRemoveFile: (LibSaveItem) -> Unit = { },
    onDismiss: () -> Unit = { }
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (saveItems.isNotEmpty()) {
            itemsIndexed(saveItems) { _, value ->
                ListItem(
                    modifier = Modifier.clickable(true) {
                        onConfirmFile(value.fileUriBox)
                        onDismiss()
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    headlineContent = { Text(text = value.fileName) },
                    trailingContent = { Text(text = PrettyTime().format(Date(value.fileLastMod))) }
                )
            }
        }

        if (menuItems.isNotEmpty()) {
            itemsIndexed(menuItems) { key, value ->
                if (value.text == "-") {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                } else {
                    val annotatedText = remember(value.text) {
                        AnnotatedString.fromHtml(value.text)
                    }
                    ListItem(
                        modifier = Modifier.clickable(true) {
                            onConfirm(key)
                            onDismiss()
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        ),
                        leadingContent = {
                            if (value.imagePath.isNotEmpty()) {
                                AsyncImage(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    model = value.imagePath,
                                    contentScale = ContentScale.Crop,
                                    contentDescription = null
                                )
                            }
                        },
                        headlineContent = {
                            Text(
                                modifier = Modifier.clearAndSetSemantics {
                                    contentDescription = annotatedText.text
                                },
                                text = annotatedText
                            )
                        }
                    )
                }
            }
        }
    }
}