package com.pixnpunk.details.presentation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixnpunk.details.presentation.R
import com.pixnpunk.dto.BinaryPrefFormat
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.utils.FileUtil.formatFileSize

@Composable
fun DetailsSecondaryInfo(
    entry: GameEntry,
    prefFormat: BinaryPrefFormat
) {
    Column(
        modifier = Modifier.padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = entry.fileSize != 0L && entry.version.isNotEmpty()
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                InfoTag(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Commit,
                            contentDescription = null
                        )
                    },
                    text = entry.version
                )
                InfoTag(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null
                        )
                    },
                    text = entry.lang
                )
                InfoTag(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null
                        )
                    },
                    text = formatFileSize(entry.fileSize, prefFormat)
                )

                AnimatedVisibility(visible = entry.pubDate.isNotEmpty()) {
                    InfoTag(text = stringResource(R.string.published_title, entry.pubDate))
                }

                AnimatedVisibility(visible = entry.modDate.isNotEmpty()) {
                    InfoTag(text = stringResource(R.string.updated_title, entry.modDate))
                }
            }
        }
    }
}