package com.pixnpunk.main.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.utils.ViewUtil.toTypeface

private fun calculateSpans(
    items: List<LibGenItem>,
    columnCount: Int,
    columnWidthPx: Float,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle,
    density: Density
): Map<Int, Boolean> {
    if (columnCount <= 1) return items.indices.associateWith { true }

    val spans = items.mapIndexed { index, item ->
        val textWidth = textMeasurer.measure(
            text = item.text,
            style = textStyle,
            maxLines = 1,
            softWrap = false
        ).size.width
        val overheadPx = with(density) {
            (if (item.imagePath.isNotBlank()) 104.dp else 32.dp).toPx()
        }
        index to (textWidth + overheadPx > columnWidthPx)
    }.toMap().toMutableMap()

    val buffer = mutableListOf<Int>()
    items.indices.forEach { index ->
        if (spans[index] == true) {
            buffer.forEach { spans[it] = true }
            buffer.clear()
        } else {
            buffer.add(index)
            if (buffer.size == columnCount) buffer.clear()
        }
    }
    buffer.forEach { spans[it] = true }

    return spans
}

private fun calculateGridHeight(
    visibleRows: Int,
    itemHeightDp: Dp = 72.dp,
    spacingDp: Dp = 8.dp,
    contentPaddingDp: Dp = 32.dp
): Dp =
    if (visibleRows <= 0) {
        0.dp
    } else {
        itemHeightDp * visibleRows + spacingDp * (visibleRows - 1) + contentPaddingDp
    }

@Composable
private fun GridCell(
    modifier: Modifier = Modifier,
    item: LibGenItem,
    isFullLine: Boolean,
    textStyle: TextStyle,
    cardBackgroundColor: Color,
    onItemClick: () -> Unit
) {
    val annotatedText = remember(item.text) { AnnotatedString.fromHtml(item.text) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = item.text.replace(Regex("<[^>]*>"), "") },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        ListItem(
            modifier = Modifier.clickable { onItemClick() },
            headlineContent = {
                Text(
                    text = annotatedText,
                    style = textStyle,
                    maxLines = if (isFullLine) 3 else 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingContent = if (item.imagePath.isNotBlank()) {
                {
                    AsyncImage(
                        model = item.imagePath,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    )
                }
            } else null,
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
fun MainContentActionsList(
    countActsVis: Int,
    settings: SettingsEntity,
    actions: List<LibGenItem>,
    onActionClicked: (Int) -> Unit,
    gridState: LazyStaggeredGridState
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val textStyle = MaterialTheme.typography.bodyLarge.merge(
            TextStyle(
                color = Color(settings.textColor),
                fontSize = settings.fontSize.sp,
                fontFamily = FontFamily(settings.typeface.toTypeface()),
                background = Color(settings.backColor)
            )
        )

        val cardBackgroundColor = Color(settings.backColor)
        val textMeasurer = rememberTextMeasurer(cacheSize = actions.size.coerceIn(1, 100))

        val columnCount = when (maxWidth) {
            in 0.dp..319.dp -> 1
            in 320.dp..549.dp -> 2
            in 550.dp..849.dp -> 3
            else -> 4
        }

        val spanMap = remember(actions, maxWidth, columnCount, density) {
            val columnWidthPx = with(density) {
                (maxWidth - 8.dp * (columnCount - 1)).toPx() / columnCount
            }
            calculateSpans(actions, columnCount, columnWidthPx, textMeasurer, textStyle, density)
        }

        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (countActsVis > 0) {
                        Modifier.heightIn(max = calculateGridHeight(countActsVis))
                    } else Modifier
                ),
            columns = StaggeredGridCells.Fixed(columnCount),
            state = gridState,
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            itemsIndexed(
                items = actions,
                key = { index, _ -> index },
                span = { index, _ ->
                    if (spanMap[index] == true) StaggeredGridItemSpan.FullLine
                    else StaggeredGridItemSpan.SingleLane
                },
            ) { index, item ->
                GridCell(
                    modifier = Modifier.animateItem(),
                    item = item,
                    isFullLine = spanMap[index] == true,
                    onItemClick = { onActionClicked(index) },
                    textStyle = textStyle,
                    cardBackgroundColor = cardBackgroundColor
                )
            }
        }
    }
}

@Preview
@Composable
private fun MainContentActionsListPreview() {
    val settings = SettingsEntity(
        textColor = 0xFFFFFFFF.toInt(),
        backColor = 0xFF000000.toInt(),
        linkColor = 0xFF00BFFF.toInt(),
        fontSize = 16,
        isUseGameFont = false,
    )
    val actions = listOf(
        LibGenItem(text = "Action 1", imagePath = ""),
        LibGenItem(text = "Action 2", imagePath = ""),
        LibGenItem(text = "Action 3", imagePath = ""),
        LibGenItem(text = "Looooooong Action 5", imagePath = "")
    )
    MainContentActionsList(
        countActsVis = 3,
        settings = settings,
        actions = actions,
        onActionClicked = {},
        gridState = rememberLazyStaggeredGridState()
    )
}
