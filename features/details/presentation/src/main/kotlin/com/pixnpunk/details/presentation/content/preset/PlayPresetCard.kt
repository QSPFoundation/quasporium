package com.pixnpunk.details.presentation.content.preset

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.pixnpunk.details.presentation.model.GameFileItem
import com.pixnpunk.details.presentation.model.PresetUIState
import com.pixnpunk.details.presentation.theme.LocalProfileColors
import com.pixnpunk.details.presentation.theme.ProfileThemePreset
import com.pixnpunk.details.presentation.theme.rememberProfileColorScheme

@Composable
fun PlayPresetCard(
    profile: PresetUIState,
    allFiles: List<GameFileItem>,
    onContinue: (savePath: String) -> Unit,
    onStartFresh: (fileName: String) -> Unit,
    onSaveVisuals: (newName: String, newColor: Long) -> Unit,
    onPickBackground: () -> Unit,
    onDeleteProfile: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var isDrawerOpen by remember { mutableStateOf(false) }

    var editedName by remember(profile.name) { mutableStateOf(profile.name) }
    var selectedColor by remember(profile.accentColor) { mutableLongStateOf(profile.accentColor) }

    val profileColors = rememberProfileColorScheme(seedColor = Color(selectedColor))
    val targetFile = profile.lastPlayedFile ?: allFiles.firstOrNull()
    val lastSave = profile.saves.firstOrNull()

    CompositionLocalProvider(LocalProfileColors provides profileColors) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = LocalProfileColors.current.cardContainer,
            contentColor = LocalProfileColors.current.onCardContainer,
            tonalElevation = if (isEditing) 8.dp else 0.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (profile.backImagePath.isNotBlank()) {
                    AsyncImage(
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(if (isEditing) 0.15f else 0.35f),
                        model = profile.backImagePath,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }

                Column {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Crossfade(
                            targetState = isEditing,
                            label = "HeaderCrossfade"
                        ) { editing ->
                            if (editing) {
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = editedName,
                                    onValueChange = { editedName = it },
                                    singleLine = true,
                                    label = { Text("Preset name") },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            } else {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    overlineContent = {
                                        Text(
                                            text = "Preset".uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color =
                                                LocalProfileColors
                                                    .current
                                                    .onCardContainer
                                                    .copy(alpha = 0.6f)
                                        )
                                    },
                                    headlineContent = {
                                        Text(
                                            text = profile.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                            }
                        }

                        AnimatedContent(
                            targetState = isEditing,
                            label = "BodyMorph"
                        ) { editing ->
                            if (!editing) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (lastSave != null) {
                                        PresetSplitButton(
                                            text = "Continue (${lastSave.displayName})",
                                            icon = Icons.Default.PlayArrow,
                                            isPrimary = true,
                                            items = profile.saves,
                                            itemLabel = { it.displayName },
                                            listIcon = Icons.Default.History,
                                            onClickMain = { onContinue(lastSave.path) },
                                            onSelectItem = { onContinue(it.path) }
                                        )
                                    }

                                    if (targetFile != null) {
                                        PresetSplitButton(
                                            text = if (lastSave != null) "Restart (${targetFile.displayName})" else "Start (${targetFile.displayName})",
                                            icon = Icons.Default.RestartAlt,
                                            isPrimary = lastSave == null,
                                            items = allFiles,
                                            itemLabel = { it.displayName },
                                            listIcon = Icons.Default.Description,
                                            onClickMain = { onStartFresh(targetFile.path) },
                                            onSelectItem = { onStartFresh(it.path) }
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "ACCENT COLOR",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = LocalProfileColors.current.onCardContainer.copy(
                                            alpha = 0.6f
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ProfileThemePreset.entries.forEach { preset ->
                                            val isSelected = selectedColor == preset.seedColor
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(preset.seedColor))
                                                    .border(
                                                        width = if (isSelected) 3.dp else 0.dp,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                        shape = CircleShape
                                                    )
                                                    .clickable {
                                                        selectedColor = preset.seedColor
                                                    }
                                            )
                                        }

                                        IconButton(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(LocalProfileColors.current.secondaryButton),
                                            onClick = onPickBackground
                                        ) {
                                            Icon(
                                                modifier = Modifier.size(18.dp),
                                                imageVector = Icons.Default.Image,
                                                contentDescription = null
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = {
                                            isEditing = false
                                        }) { Text("Cancel") }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                isEditing = false
                                                onSaveVisuals(editedName, selectedColor)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = LocalProfileColors.current.primaryButton,
                                                contentColor = LocalProfileColors.current.onPrimaryButton
                                            )
                                        ) { Text("Save") }
                                    }
                                }
                            }
                        }
                    }

                    if (!isEditing) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LocalProfileColors.current.onCardContainer.copy(alpha = 0.05f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .pointerInput(Unit) {
                                        detectVerticalDragGestures { change, dragAmount ->
                                            change.consume()
                                            if (dragAmount < -5f) isDrawerOpen = true
                                            if (dragAmount > 5f) isDrawerOpen = false
                                        }
                                    }
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        isDrawerOpen = !isDrawerOpen
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(32.dp)
                                        .height(4.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(
                                            LocalProfileColors.current.onCardContainer.copy(alpha = 0.2f)
                                        )
                                )
                            }

                            AnimatedVisibility(
                                visible = isDrawerOpen,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    TextButton(
                                        onClick = {
                                            isDrawerOpen = false
                                            isEditing = true
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = LocalProfileColors.current.onCardContainer
                                        )
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Customize",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            isDrawerOpen = false
                                            onDeleteProfile()
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Delete",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}