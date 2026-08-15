package com.pixnpunk.details.presentation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pixnpunk.details.presentation.R

@Composable
fun DetailsPrimaryInfo(
    isPlayable: Boolean,
    iconPath: String,
    title: String,
    author: String,
    portedBy: String,
    onShowEdit: () -> Unit
) {
    Column(
        modifier = Modifier.padding(4.dp)
    ) {
        Surface(shape = MaterialTheme.shapes.small) {
            ListItem(
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                leadingContent = {
                    AsyncImage(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.medium),
                        model = iconPath,
                        error = rememberVectorPainter(Icons.Outlined.BrokenImage),
                        contentDescription = stringResource(R.string.icon_description, title),
                    )
                },
                headlineContent = { Text(title) },
                supportingContent = {
                    Column {
                        AnimatedVisibility(
                            visible = author.isNotEmpty()
                        ) {
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                text = stringResource(R.string.author_title, author)
                            )
                        }
                        AnimatedVisibility(
                            visible = portedBy.isNotEmpty()
                        ) {
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                text = stringResource(R.string.ported_by_title, portedBy)
                            )
                        }
                    }
                },
                trailingContent = {
                    if (isPlayable) {
                        IconButton(
                            onClick = onShowEdit
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.modify_button_description),
                            )
                        }
                    }
                }
            )
        }
    }
}