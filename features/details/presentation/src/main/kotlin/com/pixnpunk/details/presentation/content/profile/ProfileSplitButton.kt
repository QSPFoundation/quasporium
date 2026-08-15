package com.pixnpunk.details.presentation.content.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexAlignItems
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexDirection
import androidx.compose.foundation.layout.FlexJustifyContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pixnpunk.utils.FileUtil.receiveFullName

@OptIn(ExperimentalFlexBoxApi::class)
@Composable
fun <T> ProfileSplitButton(
    isPrimary: Boolean,
    text: String,
    icon: ImageVector,
    items: List<T>,
    itemLabel: (T) -> String,
    listIcon: ImageVector,
    onClickMain: () -> Unit,
    onSelectItem: (T) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val contentColor = if (isPrimary) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ArrowRotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        color = if (isPrimary) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        contentColor = contentColor
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            FlexBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isPrimary) 56.dp else 48.dp),
                config = {
                    direction(FlexDirection.Row)
                    alignItems(FlexAlignItems.Center)
                    justifyContent(FlexJustifyContent.SpaceBetween)
                }
            ) {
                FlexBox(
                    modifier = Modifier
                        .flex {
                            grow(1f)
                            shrink(1f)
                        }
                        .fillMaxHeight()
                        .clickable { onClickMain() }
                        .padding(horizontal = 16.dp),
                    config = {
                        direction(FlexDirection.Row)
                        alignItems(FlexAlignItems.Center)
                    }
                ) {
                    Icon(
                        modifier = Modifier
                            .size(20.dp)
                            .flex { shrink(0f) },
                        imageVector = icon,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        modifier = Modifier.flex {
                            grow(1f)
                            shrink(1f)
                        },
                        text = text,
                        style = if (isPrimary) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.titleSmall
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (items.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(0.5f)
                            .background(contentColor.copy(alpha = 0.25f))
                            .flex { shrink(0f) }
                    )

                    IconButton(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .flex { shrink(0f) },
                        onClick = { isExpanded = !isExpanded }
                    ) {
                        Icon(
                            modifier = Modifier.rotate(arrowRotation),
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded && items.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(contentColor.copy(alpha = 0.08f))
                ) {
                    HorizontalDivider(
                        color = contentColor.copy(alpha = 0.15f),
                        thickness = 1.dp
                    )

                    items.forEach { item ->
                        FlexBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp)
                                .clickable {
                                    isExpanded = false
                                    onSelectItem(item)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            config = {
                                direction(FlexDirection.Row)
                                alignItems(FlexAlignItems.Center)
                            }
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(18.dp)
                                    .flex { shrink(0f) },
                                imageVector = listIcon,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                modifier = Modifier.flex {
                                    grow(1f)
                                    shrink(1f)
                                },
                                text = itemLabel(item),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}