package com.pixnpunk.details.presentation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
    val hasStats = entry.version.isNotEmpty() || entry.lang.isNotEmpty() || entry.fileSize != 0L
    val hasDates = entry.pubDate.isNotEmpty() || entry.modDate.isNotEmpty()

    AnimatedVisibility(visible = hasStats || hasDates) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasStats) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (entry.version.isNotEmpty()) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                InfoStatItem(icon = Icons.Default.Commit, text = entry.version)
                            }
                        }
                        if (entry.lang.isNotEmpty()) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                InfoStatItem(icon = Icons.Default.Language, text = entry.lang)
                            }
                        }
                        if (entry.fileSize != 0L) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                InfoStatItem(icon = Icons.Default.Code, text = formatFileSize(entry.fileSize, prefFormat))
                            }
                        }
                    }
                }

                if (hasStats && hasDates) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                if (hasDates) {
                    if (!entry.pubDate.isNotEmpty() || !entry.modDate.isNotEmpty()) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text =
                                if (entry.pubDate.isNotEmpty()) {
                                    stringResource(R.string.published_title, entry.pubDate)
                                } else {
                                    stringResource(R.string.updated_title, entry.modDate)
                                },
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = stringResource(R.string.published_title, entry.pubDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End
                            )

                            VerticalDivider(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            Text(
                                modifier = Modifier.weight(1f),
                                text = stringResource(R.string.updated_title, entry.modDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }
    }
}